public class BorrowHistory {
    private Node head;
    
    private class Node{
        BorrowRecord data;
        Node next;

        Node(BorrowRecord data){
            this.data = data;
            this.next = null;
        }
    }

    public void add(BorrowRecord record){
        Node newNode = new Node(record);

        if (head == null){
            head = newNode;
            return;
        }
        Node current = head;

        while (current.next != null){
            current = current.next;
        }

        current.next = newNode;
    }

    public void displayHistory(){
        if(head == null){
            System.out.println("No Borrowing History.");
            return;
        }
        Node current = head;

        while(current != null){
            System.out.println(current.data);
            current = current.next;
        }
    }
}
