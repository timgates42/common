/*
 * User: Timothy Gates
 * Date: Jan 6, 2002
 * Time: 1:17:05 PM
 *
 * Copyright 1995 - 2001
 * Timothy Dyson Gates
 * All Rights Reserved
 */
package com.tim.util;

import java.util.Date;
import java.text.SimpleDateFormat;

public class ActivityTraker {

	private SimpleDateFormat FULL = new SimpleDateFormat( "d MMM HH:mm:ss" );

	private int num_tasks;
	private int tasks_complete;
	private long start_time;

    public ActivityTraker() {
        this(1);
    }
    
	public ActivityTraker( int num_tasks ) {
		this.num_tasks = Math.max( 1, num_tasks );
		tasks_complete = 0;
		start_time = System.currentTimeMillis();
	}

	public void activityComplete() {
		tasks_complete++;
	}
    
    public void setEstimateUnits(int num_tasks) {
		this.num_tasks = Math.max( 1, num_tasks );
    }

	public String getCompletionEstimate() {
		long current_time = System.currentTimeMillis();
		long date = current_time + remaining( current_time );
		return FULL.format( new Date( date ) );
	}

	public String getTimeRemaining() {
		long seconds = remaining( System.currentTimeMillis() ) / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		seconds = seconds % 60;
		minutes = minutes % 60;
		StringBuffer sb = new StringBuffer();
		if ( hours > 0 ) {
			sb.append( hours );
			sb.append( " hours " );
		}
		if ( minutes > 0 ) {
			sb.append( minutes );
			sb.append( " minutes " );
		}
		sb.append( seconds );
		sb.append( " seconds" );
		return sb.toString();
	}

	private long remaining( long current_time ) {
		int tasks_left = ( num_tasks - tasks_complete );
		long total_time = current_time - start_time;
		return ( tasks_left * total_time / tasks_complete );
	}

}
