package com.tim.io;

import java.io.*;
import java.util.*;
import java.security.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import com.tim.xml.*;
import com.tim.lang.*;
import javax.xml.transform.*;


public class EncryptedVFS extends AbstractVFS {

    public static final String ENCRYPTED_FILE_SYSTEM_TYPE = "EncryptedFS";
    
    private static final int PAGESIZE = 1026;
    private static final int RESERVED_LEN = 2;
    private static final int RAW_MAX_PAGESIZE = 1024;
    private static final int ENC_MAX_PAGESIZE = PAGESIZE - RESERVED_LEN;
    
    private static final boolean DEBUG_ENCRYPTED_VFS = false;
    
    private EncryptedVFSFile root;
    
    public EncryptedVFS(VFile dir, String password) throws AuthenticationException {
        this(dir, password, false);
    }
    public EncryptedVFS(VFile dir, String password, boolean create) throws AuthenticationException {
        try {
            root = new EncryptedVFSFile(null, dir, password.getBytes(), create, true, "");
        } catch(IOException ioe) {
            if(DEBUG_ENCRYPTED_VFS) {
                System.err.println(ExceptionUtils.getStackTrace(ioe));
            }
            throw new AuthenticationException(ExceptionUtils.getStackTrace(ioe));
        }
    }
    
    public VFSFile getRoot() {
        return root;
    }
    
    public void close() {
        root = null;
    }
    
    public static String getFileName(int id) {
        return StringUtils.zpad(id, 5);
    }
    
    private static final class EncryptedVFSFile extends VFSFile {
    
        private EncryptedVFSFile parent;
        private VFile file;
        private VFile datafile;
        private long unencrypted_length;
        private byte[] password_bytes;
        private Hashtable read_cache;
        private boolean directory;
        private String name;
        private long timestamp;
        private int seqid;
        
        public EncryptedVFSFile(EncryptedVFSFile parent, VFile file, byte[] password_bytes, boolean create, boolean directory, String name) throws IOException {
            this(parent, file, password_bytes, create, directory, 0, name, System.currentTimeMillis(), 1);
        }
        
        public EncryptedVFSFile(EncryptedVFSFile parent, VFile file, byte[] password_bytes, boolean create, boolean directory, long unencrypted_length, String name, long timestamp, int seqid) throws IOException {
            this.parent = parent;
            this.file = file;
            if(directory) {
                datafile = new VFile(file, EncryptedVFS.getFileName(0));
            } else {
                datafile = file;
            }
            this.password_bytes = password_bytes;
            this.directory = directory;
            this.unencrypted_length = unencrypted_length;
            this.name = name;
            this.timestamp = timestamp;
            this.seqid = seqid;
            if(DEBUG_ENCRYPTED_VFS) {
                System.out.println("set seqid for " + file + " to " + seqid);
            }
            if(directory) {
                read_cache = new Hashtable();
                try {
                    load();
                } catch(ElementNotFoundException enfe) {
                    if(!create) {
                        throw new FileNotFoundException(ExceptionUtils.getStackTrace(enfe));
                    }
                    file.mkdir();
                    save();
                } catch(IOException ioe) {
                    if(!create) {
                        throw new FileNotFoundException(ExceptionUtils.getStackTrace(ioe));
                    }
                    file.mkdir();
                    save();
                }
            } else {
                if(!create && !file.exists()) {
                    throw new FileNotFoundException(file.getName());
                }
            }
        }
        
        public boolean supportsRandomAccess() {
            return this.datafile.supportsRandomAccess();
        }
        
        public VFSRandomAccessFile openRandomAccessFile() throws IOException {
            return new EncryptedVFSRandomAccessFile(this.datafile, getCipher(Cipher.DECRYPT_MODE), getCipher(Cipher.ENCRYPT_MODE));
        }
        
        public VFSFile getChild(String name, boolean create) throws FileNotFoundException {
            if(!isDirectory()) {
                throw new FileNotFoundException(name);
            }
            DirectoryEntry entry = (DirectoryEntry) read_cache.get(name);
            if(entry == null) {
                if(!create) {
                    throw new FileNotFoundException(name);
                }
                VFSFile vfsfile;
                try {
                    vfsfile = createFile(name, false, true);
                } catch(IOException ioe) {
                    if(DEBUG_ENCRYPTED_VFS) {
                        System.err.println(ExceptionUtils.getStackTrace(ioe));
                    }
                    throw new FileNotFoundException();
                }
                if(vfsfile == null) {
                    throw new FileNotFoundException();
                }
                return vfsfile;
            }
            return entry.getFile();
        }
        
        public InputStream read() throws IOException {
            if(isDirectory()) {
                throw new IOException("Unable to read a directory");
            }
            return getIn();
        }
        
        private InputStream getIn() throws IOException {
            return new EncryptedInputStream(this.datafile, getCipher(Cipher.DECRYPT_MODE));
            //return new CipherInputStream(datafile.read(), getCipher(Cipher.DECRYPT_MODE));
        }
        
        public OutputStream write() throws IOException {
            if(isDirectory()) {
                throw new IOException("Unable to write to a directory");
            }
            return new ByteCounterCallBackOutputStream(getOut(), this);
        }
        
        private OutputStream getOut() throws IOException {
            return new EncryptedOutputStream(this.datafile, getCipher(Cipher.ENCRYPT_MODE));
            //return new CipherOutputStream(datafile.write(), getCipher(Cipher.ENCRYPT_MODE));
        }
        
        public List listChildren() {
            return Collections.list(read_cache.keys());
        }
        
        public boolean mkdir(String name) {
            try {
                EncryptedVFSFile vfsfile = createFile(name, true, true);
                if(vfsfile != null) {
                    save();
                }
                return vfsfile != null;
            } catch(IOException ioe) {
                if(DEBUG_ENCRYPTED_VFS) {
                    System.err.println(ExceptionUtils.getStackTrace(ioe));
                }
                return false;
            }
        }
        
        public boolean delete(String name) {
            try {
                if(!isDirectory()) {
                    if(DEBUG_ENCRYPTED_VFS) {
                        System.err.println("delete " + name + "fails: is not directory.");
                    }
                    return false;
                }
                DirectoryEntry entry = (DirectoryEntry) read_cache.remove(name);
                if(entry == null) {
                    if(DEBUG_ENCRYPTED_VFS) {
                        System.err.println("delete " + name + "fails: is not found.");
                    }
                    return false;
                }
                save();
                return entry.getFile().dispose();
            } catch(IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
        }
        
        public String getName() {
            return name;
        }
        
        public long lastModified() {
            return timestamp;
        }
        
        public long length() {
            return unencrypted_length;
        }
        
        public void touchFile(long length) throws IOException {
            touchFile(length, System.currentTimeMillis());
        }
        
        public void touchFile(long length, long time) throws IOException {
            unencrypted_length = length;
            timestamp = time;
            parent.save();
        }
        
        public boolean isDirectory() {
            return directory;
        }
        
        public boolean dispose() {
            return file.delete();
        }
        
        public void save() throws IOException {
            Document document = XMLUtil.newDocument();
            Element body = XMLUtil.appendChild(document, document, "directory");
            Element data = XMLUtil.appendChild(document, body, "data");
            XMLUtil.setInt(data, "seqid", seqid);
            Enumeration keys = read_cache.keys();
            while(keys.hasMoreElements()) {
                String name = (String) keys.nextElement();
                DirectoryEntry entry = (DirectoryEntry) read_cache.get(name);
                Element entry_elem = XMLUtil.appendChild(document, body, "entry");
                entry.save(document, entry_elem);
            }
            XMLUtil.save(document, getOut());
        }
        
        public void load() throws IOException, ElementNotFoundException {
            if(DEBUG_ENCRYPTED_VFS) {
                System.err.println(IOUtils.read(getIn()));
            }
            Document document = XMLUtil.newDocument(getIn());
            Element body = XMLUtil.getElementE(document, "directory");
            Element data = XMLUtil.getElementE(body, "data");
            seqid = XMLUtil.getInt(data, "seqid", 1);
            Element[] entries = XMLUtil.getElements(body, "entry");
            for(int i = 0; i < entries.length; i++) {
                DirectoryEntry entry = new DirectoryEntry(this, file, password_bytes, entries[i]);
                read_cache.put(entry.getName(), entry);
            }
        }
        
        private Cipher getCipher(int cipher_mode) throws IOException {
            try {
                SecretKeySpec skeySpec = new SecretKeySpec(password_bytes, "Blowfish");
                Cipher cipher = Cipher.getInstance("Blowfish");
                cipher.init(cipher_mode, skeySpec);
                return cipher;
            } catch (NoSuchAlgorithmException nsae) {
                throw new IOException(nsae.getMessage());
            } catch (InvalidKeyException ike) {
                throw new IOException(ike.getMessage());
            } catch (NoSuchPaddingException nspe) {
                throw new IOException(nspe.getMessage());
            }
        }
        
        private EncryptedVFSFile createFile(String name, boolean directory, boolean create) throws IOException {
            if(read_cache.containsKey(name)) {
                return null;
            }
            int nextseqid = 1;
            synchronized(this) {
                nextseqid = seqid++;
                if(DEBUG_ENCRYPTED_VFS) {
                    System.out.println("set seqid for " + file + " to " + seqid);
                }
            }
            VFile target_file = new VFile(file, EncryptedVFS.getFileName(nextseqid));
            EncryptedVFSFile vfsfile = new EncryptedVFSFile(this, target_file, password_bytes, create, directory, name);
            DirectoryEntry entry = new DirectoryEntry(vfsfile, nextseqid);
            read_cache.put(name, entry);
            save();
            return vfsfile;
        }
        
        public int getNextSeqId() {
            return seqid;
        }
        
        private VFile getFile() {
            return file;
        }
        
        public boolean canMoveFilesTo(VFSFile dir) {
            return dir.getFileSystemType().equals(ENCRYPTED_FILE_SYSTEM_TYPE);
        }
        
        public boolean move(VFSFile src, String srcname, VFSFile dstdir, String dstname) {
            EncryptedVFSFile dst = null;
            try {
                dst = ((EncryptedVFSFile) dstdir).createFile(dstname, false, true);
                VFile srcfile = ((EncryptedVFSFile) src).getFile();
                if(DEBUG_ENCRYPTED_VFS) {
                    System.out.println(srcfile + " " + dst.getFile());
                    System.out.println(srcfile.exists() + " " + dst.getFile().exists());
                }
                srcfile.move(dst.getFile());
                dst.touchFile(src.length(), src.lastModified());
            } catch(IOException ioe) {
                if(dst != null) {
                   dstdir.delete(dst.getFile().getName());
                }
                ioe.printStackTrace();
                return false;
            }
            delete(srcname);
            return true;
        }
        
        public String getFileSystemType() {
            return ENCRYPTED_FILE_SYSTEM_TYPE;
        }
    }
    
    private static final class DirectoryEntry {
        
        private EncryptedVFSFile vfsfile;
        private int seqid;
        
        public DirectoryEntry(EncryptedVFSFile vfsfile, int seqid) {
            this.vfsfile = vfsfile;
            this.seqid = seqid;
        }
        
        public DirectoryEntry(EncryptedVFSFile parent, VFile base, byte[] password, Element element) throws FileNotFoundException {
            try {
                seqid = XMLUtil.getIntE(element, "seqid");
                String name = XMLUtil.getStringE(element, "name");
                VFile target_file = new VFile(base, EncryptedVFS.getFileName(seqid));
                boolean directory = target_file.isDirectory();
                int nextseqid = 1;
                long unencrypted_length = 0;
                if(directory) {
                    nextseqid = XMLUtil.getIntE(element, "nextseqid");
                } else {
                    unencrypted_length = XMLUtil.getLongE(element, "unencrypted_length");
                }
                long timestamp = XMLUtil.getLongE(element, "timestamp");
                vfsfile = new EncryptedVFSFile(parent, target_file, password, false, directory, unencrypted_length, name, timestamp, nextseqid);
            } catch(IOException ioe) {
                throw new FileNotFoundException();
            } catch(ElementNotFoundException enfe) {
                throw new FileNotFoundException();
            }
        }
        
        public void save(Document doc, Element element) {
            XMLUtil.setInt(element, "seqid", seqid);
            XMLUtil.setString(element, "name", vfsfile.getName());
            if(vfsfile.isDirectory()) {
                XMLUtil.setInt(element, "nextseqid", vfsfile.getNextSeqId());
            } else {
                XMLUtil.setLong(element, "unencrypted_length", vfsfile.length());
            }
            XMLUtil.setLong(element, "timestamp", vfsfile.lastModified());
        }
        
        public EncryptedVFSFile getFile() {
            return vfsfile;
        }
        
        public String getName() {
            return vfsfile.getName();
        }
        
    }
    
    private static final class ByteCounterCallBackOutputStream extends OutputStream {
        
        private OutputStream target;
        private EncryptedVFSFile callback;
        private long count;
        
        public ByteCounterCallBackOutputStream(OutputStream target, EncryptedVFSFile callback) {
            count = 0;
            this.target = target;
            this.callback = callback;
        }
        
        public void write(byte[] data, int offset, int length) throws IOException {
            count += length;
            target.write(data, offset, length);
        }
        
        public void write(int data) throws IOException {
            count++;
            target.write(data);
        }
        
        public void flush() throws IOException {
            target.flush();
        }
        
        public void close() throws IOException {
            target.close();
            callback.touchFile(count);
        }
        
    }
    
    public static final int CRYPTED_FILEBLOCK_SIZE = 4096;
    public static final int PRECRYPTED_FILEBLOCK_SIZE = 4094;
    public static final int PLAIN_FILEBLOCK_SIZE = 4092;
    public static SecureRandom SRAND = null;
    
    public static byte[] encryptBlock(Cipher cipher, byte[] in, int length) throws IOException {
        if(SRAND == null) {
            synchronized(EncryptedVFS.class) {
                if(SRAND == null) {
                    SRAND = new SecureRandom();
                }
            }
        }
        byte[] out = new byte[PRECRYPTED_FILEBLOCK_SIZE];
        short writelength = (short) length;
        if(length < PLAIN_FILEBLOCK_SIZE) {
            byte[] pad = new byte[PLAIN_FILEBLOCK_SIZE-length];
            SRAND.nextBytes(pad);
            System.arraycopy(pad, 0, out, length+2, pad.length);
        } else {
            writelength = (short) (SRAND.nextInt(Short.MAX_VALUE-PLAIN_FILEBLOCK_SIZE)+PLAIN_FILEBLOCK_SIZE);
        }
        writeShort(out, writelength);
        System.arraycopy(in, 0, out, 2, length);
        try {
            return cipher.doFinal(out);
        } catch(BadPaddingException bpe) {
            throw new IOException(bpe.getMessage());
        } catch(IllegalBlockSizeException ibse) {
            throw new IOException(ibse.getMessage());
        }
    }
    
    public static int decryptBlock(Cipher cipher, byte[] in, byte[] out) throws IOException {
        if(in.length != CRYPTED_FILEBLOCK_SIZE) {
            throw new IOException("Insufficient bytes to decrypt.");
        }
        byte[] block;
        try {
            block = cipher.doFinal(in);
        } catch(BadPaddingException bpe) {
            throw new IOException(bpe.getMessage());
        } catch(IllegalBlockSizeException ibse) {
            throw new IOException(ibse.getMessage());
        }
        int length = Math.min(readShort(block), PLAIN_FILEBLOCK_SIZE);
        System.arraycopy(block, 2, out, 0, length);
        return length;
    }
    
    public static void writeShort(byte[] data, short value) {
        data[0] = (byte)(value & 0xFF);
        data[1] = (byte)((value>>8) & 0xFF);
    }
    
    public static short readShort(byte[] data) {
        return (short)((data[0] & 0xFF) + ((data[1] & 0xFF)<<8));
    }
    
    private static final class EncryptedVFSRandomAccessFile extends PagedRandomAccessFile {
        private VFSRandomAccessFile file;
        private Cipher encrypt;
        private Cipher decrypt;
        private long vfilepos;
        private byte[] cache;
        public EncryptedVFSRandomAccessFile(VFile file, Cipher decrypt, Cipher encrypt) throws IOException {
            super(PLAIN_FILEBLOCK_SIZE);
            this.vfilepos = 0;
            this.file = file.openRandomAccessFile();
            this.decrypt = decrypt;
            this.encrypt = encrypt;
            this.cache = new byte[CRYPTED_FILEBLOCK_SIZE];
        }
        private void moveToPage(long pageid) throws IOException {
            if(this.vfilepos != pageid) {
                this.file.seek(pageid*CRYPTED_FILEBLOCK_SIZE);
                this.vfilepos = pageid;
            }
        }
        public boolean readPage(long pageid) throws IOException {
            System.out.println("read page " + pageid);
            moveToPage(pageid);
            int read = this.file.read(this.cache, 0, CRYPTED_FILEBLOCK_SIZE);
            if(read <= 0) {
                return false;
            }
            if(read != CRYPTED_FILEBLOCK_SIZE) {
                throw new IOException("Insufficient bytes to decrypt.");
            }
            if(buffer == null) {
                buffer = new byte[PLAIN_FILEBLOCK_SIZE];
            }
            bufferoffset = 0;
            usedlen = decryptBlock(decrypt, this.cache, buffer);
            this.vfilepos++;
            return true;
        }
        public void commitPage(long pageid) throws IOException {
            moveToPage(pageid);
            byte[] data = encryptBlock(encrypt, buffer, usedlen);
            if(data.length != CRYPTED_FILEBLOCK_SIZE) {
                throw new IOException("Encrypted Length Assumption Failed.");
            }
            this.file.write(data, 0, CRYPTED_FILEBLOCK_SIZE);
            this.vfilepos++;
        }
        public void doSetLength(long position) throws IOException {
            long pageid = getPageId(position);
            long pageoffset = getPageOffset(position);
            if(pageoffset > 0) {
                pageid++;
            }
            long neededdata = pageid * CRYPTED_FILEBLOCK_SIZE;
            long ofilelen = this.file.length();
            if(ofilelen > neededdata) {
                // shorten file
                this.file.setLength(neededdata);
            }
            if(ofilelen >= neededdata) {
                // truncate or grow last page if necessary
                readPage(pageid);
                usedlen = (int) Math.min(usedlen, pageoffset);
                if(usedlen < pageoffset) {
                    Arrays.fill(buffer, usedlen, (int) pageoffset, (byte)0);
                    usedlen = (int) pageoffset;
                }
                commitPage(pageid);
            } else {
                // grow file
                long pages = ofilelen / CRYPTED_FILEBLOCK_SIZE;
                readPage(pages-1);
                Arrays.fill(buffer, usedlen, PLAIN_FILEBLOCK_SIZE, (byte)0);
                usedlen = PLAIN_FILEBLOCK_SIZE;
                commitPage(pages-1);
                Arrays.fill(buffer, 0, PLAIN_FILEBLOCK_SIZE, (byte)0);
                for(int i = (int) pages; i < pageid; i++) {
                    commitPage(i);
                }
                if(pageoffset > 0) {
                    usedlen = (int) pageoffset;
                    commitPage(pageid);
                }
            }
        }
        public long length() {
            return -1;
        }
    }
    
    private static final class EncryptedOutputStream extends PagedOutputStream {
        private OutputStream out;
        private Cipher cipher;
        public EncryptedOutputStream(VFile file, Cipher cipher) throws IOException {
            super(PLAIN_FILEBLOCK_SIZE);
            this.cipher = cipher;
            this.out = file.write();    
        }
        protected void pushBuffer() throws IOException {
            byte[] data = encryptBlock(cipher, buffer, bufferlen);
            if(data.length != CRYPTED_FILEBLOCK_SIZE) {
                throw new IOException("Encrypted Length Assumption Failed. Found " + data.length + " needed " + CRYPTED_FILEBLOCK_SIZE);
            }
            this.out.write(data, 0, CRYPTED_FILEBLOCK_SIZE);
        }
        public void close() throws IOException {
            super.close();
            out.close();
        }
    }
    
    private static final class EncryptedInputStream extends PagedInputStream {
        private Cipher cipher;
        private InputStream in;
        private byte[] cache;
        public EncryptedInputStream(VFile file, Cipher cipher) throws IOException {
            this.cipher = cipher;
            this.in = file.read();
        }
        protected boolean readBuffer() throws IOException {
            System.out.println("read buffer");
            bufferoffset = 0;
            bufferlen = 0;
            if(this.cache == null) {
                this.cache = new byte[CRYPTED_FILEBLOCK_SIZE];
            }
            if(this.buffer == null) {
                this.buffer = new byte[PLAIN_FILEBLOCK_SIZE];
            }
            int read = this.in.read(this.cache, 0, CRYPTED_FILEBLOCK_SIZE);
            if(read <= 0) {
                return false;
            }
            if(read != CRYPTED_FILEBLOCK_SIZE) {
                throw new IOException("Insufficient bytes to decrypt.");
            }
            bufferoffset = 0;
            bufferlen = decryptBlock(cipher, this.cache, buffer);
            return bufferlen > 0;
        }
        public void close() throws IOException {
            super.close();
            in.close();
        }
    }
    
    private static void putFile(VFS vfs, String path) throws IOException {
        DataOutputStream out = new DataOutputStream(vfs.write(path));
        for(int i = 0; i < 40000; i++) {
            out.writeInt(i);
        }
        out.close();
    }

    private static void checkFile(VFS vfs, String path) throws IOException {
        DataInputStream in = new DataInputStream(vfs.read(path));
        for(int i = 0; i < 40000; i++) {
            int val = in.readInt();
            if(val != i) {
                System.err.println("file corrupt at " + i);
                in.close();
                return;
            }
        }
        int val = in.read();
        if(val != -1) {
            System.err.println("File too long with " + val);
        }
        in.close();
        System.out.println("File okay.");
    }
    
    private static void checkLen(VFS vfs, String path) {
        long val = vfs.length(path);
        if(val != 4*40000) {
            System.err.println("Bad File length " + val);
        }
    }

    private static void checkDir(VFS vfs, String path, String[] elems) {
        try {
            String[] cont = vfs.list(path);
            List srclist = Arrays.asList(cont);
            List dstlist = Arrays.asList(elems);
            Collections.sort(srclist);
            Collections.sort(dstlist);
            if(!srclist.equals(dstlist)) {
                System.err.println("Bad File List " + srclist + " is not " + dstlist + ",");
            }
        } catch(IOException ioe) {
            System.err.println(ioe);
        }
    }

    public static void main(String[] args) {
        try {
            VFile dir = new VFile(MasterVFS.getFileSystem(), args[0]);
            String password = args[1];
            EncryptedVFS vfs = new EncryptedVFS(dir, password, true);
            vfs.mkdir("/x");
            checkDir(vfs, "/x", new String[0]);
            putFile(vfs, "/x/1");
            checkFile(vfs, "x/1");
            checkLen(vfs, "/x/1");
            checkDir(vfs, "/x", new String[] { "1" });
            vfs.move("/x/1", "/x/2");
            checkLen(vfs, "/x/2");
            checkDir(vfs, "/x", new String[] { "2" });
            checkFile(vfs, "x/2");
            vfs.copy("/x/2", "/x/1");
            checkDir(vfs, "/x", new String[] { "1", "2" });
            checkFile(vfs, "x/2");
            checkLen(vfs, "/x/2");
            vfs.delete("/x/2");
            checkDir(vfs, "/x", new String[] { "1" });
            checkFile(vfs, "x/1");
            checkLen(vfs, "/x/1");
            vfs.delete("/x/1");
            checkDir(vfs, "/x", new String[0]);
            vfs.delete("/x");
            vfs.mkdir("/x");
            VFSRandomAccessFile rfile = vfs.openRandomAccessFile("/x/1");
            byte[] data = "This is a test".getBytes();
            rfile.write(data, 0, data.length);
            rfile.seek(2);
            byte[] buffer = new byte[1];
            rfile.read(buffer, 0, 1);
            /*
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(vfs.write("/x/c")));
            writer.println("hello");
            writer.close();
            System.out.println(IOUtils.read(vfs.read("/x/c")));
            System.out.println(vfs.length("/x/c"));
            */
        } catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static final class CipherPagedOutputStream extends PagedOutputStream {

        private DataOutputStream out;
        private Cipher cipher;
        private SecretKeySpec keySpec;
        
        public CipherPagedOutputStream(OutputStream out, Cipher cipher, SecretKeySpec keySpec) {
            super(RAW_MAX_PAGESIZE);
            this.out = new DataOutputStream(out);
            this.cipher = cipher;
            this.keySpec = keySpec;
        }

        protected void pushBuffer() throws IOException {
            try {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
                byte[] output = cipher.doFinal(buffer);
                if(output.length > ENC_MAX_PAGESIZE) {
                    throw new RuntimeException("Maximum encrypted page size exceeded, was " + buffer.length + ".");
                }
                byte[] page = new byte[PAGESIZE];
                writeShort(page, 0, (short) output.length); 
                System.arraycopy(output, 0, page, RESERVED_LEN, output.length);
                out.write(page);
                bufferlen = 0;
            } catch(GeneralSecurityException gse) {
                throw new IOException("Invalid Authentication.");
            }
        }
        
        public void close() throws IOException {
            super.close();
            out.close();
        }
        
        private void writeShort(byte[] data, int offset, short value) {
            data[offset] = (byte)((value>>8)&0xFF);
            data[offset+1] = (byte)(value&0xFF);
        }
        
    }
    
    private static final class CipherPagedInputStream extends PagedInputStream {
        
        private DataInputStream in;
        private Cipher cipher;
        private SecretKeySpec keySpec;
        
        public CipherPagedInputStream(InputStream in, Cipher cipher, SecretKeySpec keySpec) {
            this.in = new DataInputStream(in);
            this.cipher = cipher;
            this.keySpec = keySpec;
        }

        public void close() throws IOException {
            super.close();
            in.close();
        }
        
        protected boolean readBuffer() throws IOException {
            try {
                if(buffer == null) {
                    buffer = new byte[PAGESIZE];
                }
                int len = in.read(buffer, 0, PAGESIZE); 
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                if(len == -1) {
                    return false;
                }
                if(len <= RESERVED_LEN) {
                    throw new IOException("Invalid File Format");
                }
                int bufferlen = readShort(buffer, 0);
                bufferoffset = RESERVED_LEN;
                cipher.doFinal(buffer, bufferoffset, bufferlen);
                if(bufferlen+bufferoffset>len) {
                    throw new IOException("Invalid File Format");
                }
                return true;
            } catch(GeneralSecurityException gse) {
                throw new IOException("Invalid Authentication.");
            }
        }
        
        private short readShort(byte[] data, int offset) {
            return (short) (((int)(data[offset]&0xFF)) << 8 + ((int)(data[offset+1]&0xFF)));
        }
    }
    
}


