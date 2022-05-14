/* Master of a basic master/worker example in Java */

/* Copyright (c) 2006-2022. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package app.masterworker2;
import org.simgrid.msg.Host;
import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Task;
import org.simgrid.msg.Process;

//imported these
import java.util.Arrays;

public class Master extends Process {
  public Master(Host host, String name, String[]args) {
    super(host,name,args);
  }
  public void main(String[] args) throws MsgException {
    if (args.length < 4) {
      Msg.info("Master needs 4 arguments");
      System.exit(1);
    }

    int tasksCount = Integer.parseInt(args[0]);
    double[] taskComputeSize = Arrays.stream(args[1].split(",")).mapToDouble(Double::parseDouble).toArray(); //string to double array
    double taskCommunicateSize = Double.parseDouble(args[2]);

    int workersCount = Integer.parseInt(args[3]);

    long[] starttimes = new long[workersCount];
    long[] endtimes = new long[workersCount];

    Msg.info("Hello! My PID is "+getPID()+". Got "+  workersCount + " workers and "+tasksCount+" tasks to process");
    
    Task[] temp = new Task[tasksCount]; // task creator
    for (int i = 0; i < tasksCount; i++) {
      Task task = new Task("Task_" + i, taskComputeSize[i], taskCommunicateSize);
      temp[i] = task;
    }

    Arrays.sort(temp, (a, b) -> (int)a.getFlopsAmount() - (int)b.getFlopsAmount()); //weird sort thing...
    long start = System.nanoTime();
    for (int i = 0; i < tasksCount; i++) {
      Msg.debug("Sending \"" + temp[i]+ "\" to \"worker_" + i % workersCount + "\"");
      temp[i].send("worker_"+(i%workersCount));
    }
    long end = System.nanoTime();

    Msg.info("All tasks have been dispatched. Let's tell everybody the computation is over.");

    for (int i = 0; i < workersCount; i++) {
      Task task = new Task("finalize", 0, 0);
      task.send("worker_"+(i%workersCount));
    }

    Msg.info("Goodbye now!");
    System.out.println("Total Time: " + (end-start));
  }

  public static double avgArr(long[] A){
    double avg = 0;
    for(int i = 0; i < A.length; ++i){
      avg += A[i];
    }
    avg = avg/A.length;
    return avg;
  }

}

