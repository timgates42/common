package com.tim.awt;

import java.awt.*;
import javax.swing.*;

public class UGridLayout implements LayoutManager {

    private int rows;
    private int cols;
    private int hpad;
    private int vpad;

    public UGridLayout() {
        this(0, 1);
    }

    public UGridLayout(int rows, int cols) {
        this(rows, cols, 0, 0);
    }

    public UGridLayout(int rows, int cols, int hpad, int vpad) {
        this.rows = rows;
        this.cols = cols;
        this.hpad = hpad;
        this.vpad = vpad;
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        return traverseComponents(parent, new Delegate());
    }

    public Dimension minimumLayoutSize(Container parent) {
        return traverseComponents(parent, new MinimumDelegate());
    }
    
    public void layoutContainer(Container parent) {
        traverseComponents(parent, new LayoutDelegate());
    }

    public Dimension  traverseComponents(Container parent, Delegate control) {
        synchronized (parent.getTreeLock()) {
            int ncomponents = parent.getComponentCount();
            int nrows = rows;
            int ncols = cols;

            if (nrows > 0) {
                ncols = (ncomponents + nrows - 1) / nrows;
            } else {
                nrows = (ncomponents + ncols - 1) / ncols;
            }
            control.init(nrows, ncols);
            int w = 0;
            int h = 0;
            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                control.addComponent(comp, i / ncols, i % ncols);
            }
            return control.run(parent, hpad, vpad);
        }
	}
    
    private static class Delegate {
        protected int[] col_data;
        protected int[] row_data;
        public void init(int rows, int cols) {
            col_data = new int[cols];
            row_data = new int[rows];
        }
        public void addComponent(Component comp, int row, int col) {
            addToRow(comp, row);
            addToCol(comp, col);
        }
        protected void addToRow(Component comp, int row) {
            row_data[row] = Math.max(getData(comp).height, row_data[row]);
        }
        protected Dimension getData(Component comp) {
            return comp.getPreferredSize();
        }
        protected void addToCol(Component comp, int col) {
            col_data[col] = Math.max(getData(comp).width, col_data[col]);
        }
        public Dimension run(Container parent, int hgap, int vgap) {
            return getSize(parent, hgap, vgap);
        }
        public Dimension getSize(Container parent, int hgap, int vgap) {
            Insets insets = parent.getInsets();
            return new Dimension(insets.left + insets.right + sum(col_data) + (col_data.length-1)*hgap, 
			     insets.top + insets.bottom + sum(row_data) + (row_data.length-1)*vgap);
        }
        protected int sum(int[] data) {
            int sum = 0;
            for(int i = 0; i < data.length; i++) {
                sum += data[i];
            }
            return sum;
        }
    }
    
    private static class MinimumDelegate extends Delegate {
        protected Dimension getData(Component comp) {
            return comp.getMinimumSize();
        }
    }
    
    private static class LayoutDelegate extends Delegate {
        private boolean use_minimum;
        protected Dimension getData(Component comp) {
            if(!use_minimum) {
                return comp.getPreferredSize();
            }
            return comp.getMinimumSize();
        }
        public Dimension run(Container parent, int hgap, int vgap) {
            Dimension psize = parent.getSize();
            Dimension source = getSize(parent, hgap, vgap);
            if(source.width > psize.width || source.height > psize.height) {
                use_minimum = true;
                source = getSize(parent, hgap, vgap);
            }
            Insets insets = parent.getInsets();
            int hstatic = insets.left + insets.right + (col_data.length-1)*hgap;
            int vstatic = insets.top + insets.bottom + (row_data.length-1)*vgap;
            double hratio = ((double) (psize.width - hstatic)) / ((double) (source.width - hstatic));
            double vratio = ((double) (psize.height - vstatic)) / ((double) (source.height - vstatic));
            int ncomponents = parent.getComponentCount();
            int y = insets.top;
            int ncomp = 0;
            for(int row = 0; row < row_data.length; row++) {
                int height = (int) (row_data[row]*vratio); 
                int x = insets.left;
                for(int col = 0; col < col_data.length; col++) {
                    if(ncomp < ncomponents) {
                        int width = (int) (col_data[col]*hratio);
                        Component comp = parent.getComponent(ncomp);
                        comp.setBounds(x, y, width, height);
                        x += width + hgap;
                    }
                    ncomp++;
                }
                y += height + vgap;
            }
            return null;
        }
    }
    
    public static void main(String[] args) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new BorderLayout());
        JPanel north = new JPanel(new BorderLayout());
        JPanel east = new JPanel(new BorderLayout());
        JPanel west = new JPanel(new UGridLayout(2,2));
        west.add(new JLabel("Short String"));
        west.add(new JLabel("Long String........................"));
        west.add(new JLabel("Short String"));
        west.add(new JLabel("Long String........................"));
        panel.add(center, BorderLayout.CENTER);
        panel.add(north, BorderLayout.NORTH);
        north.add(west, BorderLayout.WEST);
        north.add(east, BorderLayout.CENTER);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.setBounds(500, 500, 200, 200);
        frame.setVisible(true);
    }

}
