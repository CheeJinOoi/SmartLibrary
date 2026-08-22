public class MaxHeap {
    private OverdueBook[] heap;
    private int size;
    public MaxHeap(int capacity){
        heap = new OverdueBook[capacity];
        size = 0;
    }
    public void insert(OverdueBook overdueBook){
        if(size == heap.length){
            System.out.println("Heap is full.");
            return;
        }
        heap[size] = overdueBook;
        int current = size;
        size++;
        heapifyUp(current);
    }
    private void heapifyUp(int index){
        while(index>0){
            int parent = (index -1)/2;
            if(heap[parent].getDaysOverdue() >= heap[index].getDaysOverdue()){
                break;
            }
            swap(parent, index);
            index = parent;
        }
    }
    private void swap(int first, int second){
        OverdueBook temp = heap[first];
        heap[first] = heap[second];
        heap[second] = temp;
    }
    public OverdueBook peek(){
        if(size ==0){
            return null;
        }
        return heap[0];
    }
    public OverdueBook extractMax(){
        if(size ==0){
            return null;
        }
        OverdueBook result = heap[0];
        heap[0] = heap[size -1];
        heap[size - 1] = null;
        size--;
        heapifyDown(0);
        return result;
    }
    private void heapifyDown(int index){
        while(true){
            int left = 2* index+1;
            int right = 2* index+2;
            int largest = index;
            if(left < size && heap[left].getDaysOverdue() > heap[largest].getDaysOverdue()){
                largest = left;
            }
            if(right < size && heap[right].getDaysOverdue() > heap[largest].getDaysOverdue()){
                largest = right;
            }
            if(largest == index){
                break;
            }
            swap(index, largest);
            index = largest;
        }
    }
    public boolean isEmpty(){
        return size == 0;
    }
}
