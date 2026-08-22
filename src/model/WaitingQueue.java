public class WaitingQueue {
    private Node front;
    private Node rear;

    public class Node{
        String studentName;
        Node next;

        Node(String studentName){
            this.studentName = studentName;
            this.next = null;
        }
    }
    public void enqueue(String studentName){
        Node newNode = new Node(studentName);
        if(rear == null){
            front = newNode;
            rear = newNode;
            return;
        }
        rear.next = newNode;
        rear = newNode;
    }
    public String dequeue(){
        if(front == null){
            return null;
        }
        String studentName = front.studentName;
        front = front.next;
        if(front == null){
            rear = null;
        }
        return studentName;
    }

    public void displayQueue(){
        if(front == null){
            System.out.println("Waiting list is empty");
            return;
        }
        Node current = front;

        while (current!= null){
            System.out.println(current.studentName);
            current = current.next;
        }
    }

    public int size(){
        int count = 0;
        Node current = front;
        while(current != null){ count++; current = current.next; }
        return count;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public java.util.ArrayList<String> toList() {
        java.util.ArrayList<String> result = new java.util.ArrayList<>();
        Node current = front;
        while (current != null) {
            result.add(current.studentName);
            current = current.next;
        }
        return result;
    }
}
