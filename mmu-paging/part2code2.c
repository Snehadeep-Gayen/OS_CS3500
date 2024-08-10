#include <stdio.h>
#include <stdlib.h>

int main(){
    int* data = (int*) malloc(sizeof(int)*100);
    if(data == NULL){
        printf("Memory Allocation failed\n");
        return 0;
    }
    free(data);
    printf("%d\n", data[50]);
    return 0;
}

