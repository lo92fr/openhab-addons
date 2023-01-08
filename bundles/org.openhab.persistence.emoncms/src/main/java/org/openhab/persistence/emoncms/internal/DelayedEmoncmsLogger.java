/**
 * Copyright (c) 2015, Inria.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nicolas Bonnefond - initial API and implementation and/or initial documentation
 */
package org.openhab.persistence.emoncms.internal;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.HashMap;

import org.openhab.core.items.Item;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.types.UnDefType;
import org.quartz.DateBuilder;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the Emoncms {@link PersistenceService}.
 * 
 * @author Nicolas Bonnefond (nicolas.bonnefond@inria.fr)
 */
public final class DelayedEmoncmsLogger extends EmoncmsLogger{


	private static final Logger logger = LoggerFactory
			.getLogger(DelayedEmoncmsLogger.class);
	
	private int sendInterval;
	private static final HashMap<String, DecimalType> datas = new HashMap<String, DecimalType>();;
	private static boolean bulking;
	private JobDetail job;
	

	public DelayedEmoncmsLogger(String econcmsUrl, String apiKey, int node,
			boolean round , int sendInterval) {
		super(econcmsUrl, apiKey, node, round);
		this.sendInterval = sendInterval;
		bulking = false;
		job = newJob(EmoncmsSendJob.class).build();
	}
	
	public void logEvent(Item item) {
		
		synchronized (datas) {			

		try {
			if (!(item.getState() instanceof UnDefType)) {
				datas.put(item.getName(),  (DecimalType) item.getStateAs(DecimalType.class));
					if (!bulking){
						bulking = true;
					Scheduler sched = StdSchedulerFactory.getDefaultScheduler();
					Trigger trigger = newTrigger().startAt(DateBuilder.futureDate(sendInterval, IntervalUnit.MILLISECOND)).build();

						sched.scheduleJob(job, trigger);
					
						logger.debug("Scheduled Commit-Job with interval {}ms.", this.sendInterval);
					}} else {
				logger.debug("emoncms logger : object " + item.getName()
						+ " Uninitialized");
			}
		} catch (SchedulerException e) {
			logger.error("emoncms logger error : " + e.getLocalizedMessage());
		}
	}}
	

	public static class EmoncmsSendJob implements Job {

		
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			synchronized (datas) {

			logger.debug("Execute emoncmsSendJob");
			try {
					
			postDatas((HashMap<String, DecimalType>) datas);
			datas.clear();
			bulking = false;
			} catch (Exception e) {
				logger.error("error posting datas : " + e.getLocalizedMessage());
				bulking = false;
			}
			}
		
	}}


}
