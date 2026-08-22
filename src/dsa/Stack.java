public class Stack {
    private Node top;
    private class Node{
        Action data;
        Node next;

        Node(Action data){
            this.data= data;
            this.next = null;
        }
    }
    public void push(Action action){
        Node newNode = new Node(action);
        newNode.next = top;
        top = newNode;
    }
    public Action pop(){
        if(top == null){
            return null;
        }
        Action action = top.data;
        top = top.next;
        return action;
    }
    public Action peek(){
        if(top == null){
            return null;
        }
        return top.data;
    }
    public boolean isEmpty(){
        return top == null;
    }
}
