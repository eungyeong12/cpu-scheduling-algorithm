import java.util.*;

public class SJF {
    public SJF() {}
    public List<Result> run(List<Process> jobList, List<Result> resultList) {
        int currentProcess = 0;
        int cpuTime = 0;
        int cpuDone = 0;
        int runTime = 0;

        Map<Integer, Integer> map = new HashMap<>();
        for(Process p : jobList) {
            map.put(p.processID, p.arriveTime);
        }

        List<ReadyQueueElement> readyQueue = new ArrayList<>();
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
                    if(readyQueue.size() > 1) {
                        readyQueue.sort(new Comparator<ReadyQueueElement>() {
                            @Override
                            public int compare(ReadyQueueElement o1, ReadyQueueElement o2) {
                                return o1.burstTime - o2.burstTime;
                            }
                        });
                    }
                    ReadyQueueElement rq =readyQueue.get(0);
                    int turnaroundTime = (runTime+rq.burstTime-map.get(rq.processID));
                    int responseTime = runTime-map.get(rq.processID);
                    resultList.add(new Result(rq.processID, runTime, rq.burstTime, rq.waitingTime, turnaroundTime, responseTime));
                    cpuDone = rq.burstTime;
                    cpuTime = 0;
                    currentProcess = rq.processID;
                    readyQueue.remove(0);
                }
            } else {
                if(cpuTime == cpuDone) {
                    currentProcess = 0;
                    continue;
                }
            }

            cpuTime++;
            runTime++;

            for(int i=0; i<readyQueue.size(); i++)
                readyQueue.get(i).waitingTime++;

        } while(jobList.size() != 0 || readyQueue.size() != 0 || currentProcess != 0);
        return resultList;
    }
}
