import java.util.*;

public class RR {
    int timeSlice;

    public RR(int timeSlice) {
        this.timeSlice = timeSlice;
    }
    public List<Result> run(List<Process> jobList, List<Result> resultList) {
        int currentProcess = 0;
        int cpuTime = 0;
        int cpuDone = 0;
        int runTime = 0;
        int index = 0;
        boolean d = false;
        List<ReadyQueueElement> readyQueue = new ArrayList<>();

        Map<Integer, Integer> map = new HashMap<>();
        for(Process p : jobList) {
            map.put(p.processID, p.arriveTime);
        }

        do {
            while(jobList.size() != 0) {
                for(int i=0; i<jobList.size(); i++) {
                    if(jobList.get(i).arriveTime == runTime) {
                        readyQueue.add(new ReadyQueueElement(jobList.get(i).processID, jobList.get(i).burstTime, 0, jobList.get(i).priority));
                        jobList.remove(i);
                    }
                }
                break;
            }

            if(currentProcess == 0) {
                if(readyQueue.size() != 0) {
                    d = false;
                    ReadyQueueElement rq = readyQueue.get(index);
                    Result result;
                    if(rq.burstTime > timeSlice) {
                        int turnaroundTime = (runTime+timeSlice-map.get(rq.processID));
                        int responseTime = runTime-map.get(rq.processID);
                        result = new Result(rq.processID, runTime, timeSlice, rq.waitingTime, turnaroundTime, responseTime);
                        resultList.add(result);
                        cpuDone = timeSlice;
                        rq.burstTime -= timeSlice;
                    } else {
                        int turnaroundTime = (runTime+rq.burstTime-map.get(rq.processID));
                        int responseTime = runTime-map.get(rq.processID);
                        result = new Result(rq.processID, runTime, rq.burstTime, rq.waitingTime, turnaroundTime, responseTime);
                        resultList.add(result);
                        cpuDone = rq.burstTime;
                        readyQueue.remove(rq);
                        d = true;
                    }

                    currentProcess = rq.processID;
                    cpuTime = 0;

                }
            } else {
                if(cpuTime == cpuDone) {
                    if(!d) index++;
                    if(index >= readyQueue.size())
                        index = 0;

                    currentProcess = 0;

                    continue;
                }
            }

            cpuTime++;
            runTime++;

            for(int i=0; i<readyQueue.size(); i++) {
                if(readyQueue.get(i).processID == currentProcess) continue;
                readyQueue.get(i).waitingTime++;
            }

        } while(jobList.size() != 0 || readyQueue.size() != 0 || currentProcess != 0);
        return resultList;
    }
}
