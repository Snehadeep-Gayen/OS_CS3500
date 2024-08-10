#include <iostream>
#include <list>
#include <functional>
#include <vector>

/*/////////ASSUMPTIONS///////////////

1. Ties are broken according to PID 
(lower pid first)
2. If threshold time of process A ends
before that of B then A gets promoted 
before B
3. There are no empty lines / extra 
lines in the input file

/////////////////////////////////*/

class Job{
    int pid;
    int startTime;
    int burstTime;
    int endTime;
    int waitTime;
    int runTime;
    int startLevel;
    int currentLevel;
    int queueStartTime;
public:
    Job(int _id, int _startTime, int _burstTime, int sLevel = 1) 
        : pid(_id), startTime(_startTime), burstTime(_burstTime), startLevel(sLevel) {
        endTime = -1;
        currentLevel = startLevel;
        waitTime = runTime = 0;
        queueStartTime = 0;
    }

    void printStatus(){
        if(endTime==-1) 
            std::cout<<pid<<":running; Time left: "<<burstTime<<"\n";
        else 
            printf("ID: %d; Level: %d; Final Level: %d; Comp. Time(ms): %d; TAT (ms): %d\n",
                pid, startLevel, currentLevel, endTime, endTime-startTime);
    }

    int runOnce(int time){
        if(endTime!=-1) return -1;
        burstTime--;
        runTime++;
        if(burstTime==0){
            endTime = time;
            return 1;
        }
        return 0;
    }

    void waitOnce(){
        waitTime++;
        runTime=0;
    }

    void promote(){
        if(currentLevel<4)
            currentLevel++;
        waitTime = 0;
        runTime = 0;
    }

    bool isComplete() const{ return endTime!=-1; }
    void clearRunTime(){ runTime = 0; }
    bool hasWaited(int threshold) const{ return waitTime >= threshold; }
    bool hasRun(int threshold) const{  return runTime >= threshold; }
    int getBurstTime() const { return burstTime; }
    int getTAT() { return endTime-startTime; }
    int getQueueStartTime() { return queueStartTime; }
    void setQueueStartTime(int time) { queueStartTime = time; }
    int getPID() const { return pid; }
    int getStartTime() const { return startTime; }
    int getStartLevel() const { return startLevel; }
};

typedef std::list<Job*> jobList;

class JobList{
    jobList jl;
    Job* active;
    std::function<jobList::iterator(jobList&)> choose;

public: 
    JobList() : jl(), choose() { active = nullptr; }

    void setFunction(std::function<jobList::iterator(jobList&)> f){
        choose = f;
    }

    void addJob(Job* j, int time){
        j->setQueueStartTime(time);
        jl.push_back(j);
    }

    void removeJob(Job* j){
        for(auto itr=jl.begin(); itr!=jl.end(); itr++)
            if(*itr==j){
                jl.erase(itr);
                return;
            }
    }

    bool isRunning() const{ return active!=nullptr; }

    void runOnce(int time){
        if(active==nullptr) return;
        active->runOnce(time);
        if(active->isComplete()){
            active->printStatus();
            active = nullptr;
        }
        for(auto i : jl) i->waitOnce();
    }

    void increaseWait(){  for(auto j : jl) j->waitOnce(); }

    std::list<Job*> removeExceeded(int threshold){
        std::list<Job*> answer;
        for(auto itr=jl.begin(); itr!=jl.end();)
            if((*itr)->hasWaited(threshold)){
                answer.push_back(*itr);
                auto itr2 = itr; itr2++;
                jl.erase(itr);
                itr = itr2;
            }
            else
                itr++;
        return answer;
    }

    bool activeExceeded(int rrTime){ return active->hasRun(rrTime); }

    bool activate(){
        if(jl.empty()) return false;
        auto nextJobitr = choose(jl);
        active = *nextJobitr;
        jl.erase(nextJobitr);
        return true;
    }

    Job* removeActive(){
        auto current = active;
        active = nullptr;
        current->clearRunTime();
        return current;
    }

    void printList(){
        for(auto j : jl) std::cout<<j->getPID()<<" ";
        std::cout<<"\n";
    }
};

jobList::iterator firstJob(jobList& l){
    int leastTime = INT_MAX;
    int pid = INT_MAX;
    auto ans = l.end();
    for(auto jitr = l.begin(); jitr!=l.end(); jitr++){
        int thistime = (*jitr)->getQueueStartTime();
        int thispid = (*jitr)->getPID();
        if( thistime<leastTime || 
            (thistime==leastTime && thispid<pid)
        ){
            leastTime = thistime;
            pid = thispid;
            ans = jitr;
        }
    }
    return ans;
}

jobList::iterator shortestJob(jobList& l){
    int leastBurst = INT_MAX;
    int pid = INT_MAX;
    auto ans = l.end();
    for(auto jitr = l.begin(); jitr!=l.end(); jitr++){
        int thistime = (*jitr)->getBurstTime();
        int thispid = (*jitr)->getPID();
        if( thistime<leastBurst || 
            (thistime==leastBurst && thispid<pid)
        ){
            leastBurst = thistime;
            pid = thispid;
            ans = jitr;
        }
    }
    return ans;
}

class MLFQ{
    JobList* qs;
    int time;
    int rrTime;
    int demoteTime;

public:
    MLFQ(int rr, int d) : rrTime(rr), demoteTime(d) {
        qs = new JobList[5];
        qs[4].setFunction(&firstJob);
        qs[3].setFunction(&shortestJob);
        qs[2].setFunction(&shortestJob);
        qs[1].setFunction(&firstJob);
        time = 0;
    }

    void addJob(Job* j, int level){
        if(level<=0 || level>=5) return;
        qs[level].addJob(j, time);
    }

    bool increaseTime(){
        time++;
        // std::cout<<"---"<<time<<"---\n";
        // std::cout<<"Q1: "; qs[1].printList();
        // std::cout<<"Q2: "; qs[2].printList();
        // std::cout<<"Q3: "; qs[3].printList();
        // std::cout<<"Q4: "; qs[4].printList();

        // check if some job was running
        int ran = 0;
        for(int i=4; i>=1; i--)
            if(qs[i].isRunning()){
                qs[i].runOnce(time);
                ran = i;
            }
        if(!ran)
            for(int i=4; i>=1; i--)
                if(qs[i].activate()){
                    qs[i].runOnce(time);
                    ran = i;
                    break;
                }
        if(!ran) // no process in any queue
            return false;
        // wait all the other processes
        for(int i=1; i<=4; i++)
            if(i!=ran)
                qs[i].increaseWait();
        // check what all processes have exceeded threshold
        for(int i=1; i<=3; i++){
            auto removedJobs = qs[i].removeExceeded(demoteTime);
            for(auto j : removedJobs){
                j->promote();
                qs[i+1].addJob(j, time);
            }
        }

        // check if round robin time is exceeded
        if(qs[4].isRunning() && qs[4].activeExceeded(rrTime)){
            qs[4].addJob(qs[4].removeActive(), time);
        }
        return true;
    }
};


int main(int argc, char* argv[])
{
    int roundRobinTime, timeinQueue;
    int t = 0;
    // std::cin>>roundRobinTime>>timeinQueue;

    if(argc==5){
       roundRobinTime = atoi(argv[1]);
       timeinQueue = atoi(argv[2]);
       if(freopen(argv[3], "r", stdin)==nullptr){
        std::cerr<<"Cannot open input file "<<std::string(argv[3])<<"\n";
        exit(0);
       }
       if(freopen(argv[4], "w", stdout)==nullptr){
        std::cerr<<"Cannot open output file "<<std::string(argv[4])<<"\n";
        exit(0);
       }
    }
    else{
        std::cerr<<"usage: <program> <rrtime> <qtime> <input> <output>\n";
        exit(0);
    }

    MLFQ myf(roundRobinTime, timeinQueue);

    std::vector<Job*> jobArray;
    
    while(!std::cin.eof()){
        int name, start, burst, level;
        std::cin>>name>>level>>start>>burst;
        Job* j = new Job(name, start, burst, level);
        jobArray.push_back(j);
    }

    std::sort(jobArray.begin(), jobArray.end(), [](Job* j1, Job* j2){ 
        return j1->getStartTime() < j2->getStartTime() || 
            (j1->getStartTime() == j2->getStartTime() && j1->getPID() < j2->getPID());
        });
    

    for(auto job : jobArray){
        while(t < job->getStartTime()){
            t++;
            myf.increaseTime();
        }
        myf.addJob(job, job->getStartLevel());
    }

    while(myf.increaseTime()) t++;

    long long int TAT = 0;
    for(auto j : jobArray){
        TAT += j->getTAT();
        delete j;
    }

    int njobs = jobArray.size();
    printf("Mean Turnaround Time: %.2f (ms); Throughput: %.2f processes/sec\n",
            static_cast<double>(TAT*1.0)/njobs, 1000.0*njobs/t);

    std::cout.flush();
    return 0;
}