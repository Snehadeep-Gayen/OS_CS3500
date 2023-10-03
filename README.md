# Lab 4 - Take Home
# Name: Snehadeep Gayen
# Roll Number: CS21B078

1. All code is written solely by me.
2. # How to run
   1. `rm -r *.class`
   2. `javac *.java`
   3. `java Global ...` replace `...` with your parameters
3. There are no errors (appropriate exceptions are thrown) or weaknesses in the code
4. ### Errors thrown by the program 
   #### Errors in `load` method:
- Memory is full
- File does not exist
- Loaded into physical memory
- Loaded into virtual memory

#### Errors in `pteAll` method:
- Argument not provided

#### Errors in `pte` method:
- Invalid PID
- File error
- Successfully executed

#### Errors in `swapin` method:
- Invalid PID
- Process not in Virtual Memory
- Not Enough Space in Physical Memory
- Successfully executed

#### Errors in `swapout` method:
- Invalid PID
- Process not in Physical Memory
- Not Enough Space in Virtual Memory
- Successfully executed

#### Errors in `kill` method:
- Invalid PID
- Successfully killed the process

#### Errors in `run` method:
- Invalid PID
- Cannot fit process in main memory
- Running process
- Running process

#### Errors in `print` method:
- None

#### Errors in `listpr` method:
- None

5. Assumptions 
   1. Since there is no overflow mentioned, I've assigned an int to every memory location instead of byte
   2. If processes s1, s2, ... have been swapped to virtual mem in order accomodate a process s. Then they won't be brought back (irrespective of whether process s is actually able to fit or not)
   3. The LRU algorithm keeps removing process (and skips over "large" processes that cannot fit in Virtual Memory) till the required process can be fit into memory.
   4. Processes that haven't run even once, will get evicted last
   5. Processes are not killed if invalid memory is accessed. It just stops running.
6. Specifications of my code
   1. If some file is not writeable in any command, it skips that file and goes to the next command