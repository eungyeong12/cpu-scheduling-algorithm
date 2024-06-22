import java.util.*;

public class FCFS {
    public FCFS() {}
    public List<Result> run(List<Process> jobList, List<Result> resultList) {
        int currentProcess = 0;
        int cpuTime = 0;
        int cpuDone = 0;
        int runTime = 0;

        List<ReadyQueueElement> readyQueue = new ArrayList<>();

        Map<Integer, Integer> map = new HashMap<>();
        for(Process p : jobList) {
            map.put(p.processID, p.arriveTime);
        }

        jobList.sort(new Comparator<Process>() {
            @Override
            public int compare(Process o1, Process o2) {
                return o1.arriveTime - o2.arriveTime;
            }
        });

        do {
            while(jobList.size() != 0) {
                Process frontJob = jobList.get(0);
                if(frontJob.arriveTime == runTime) {
                    readyQueue.add(new ReadyQueueElement(frontJob.processID, frontJob.burstTime, 0, frontJob.priority));
                    jobList.remove(0);
                } else {
                    break;
                }
            }

            if(currentProcess == 0) {
                if(readyQueue.size() != 0) {
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
