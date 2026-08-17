import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class BookGraph {
    private HashMap<String, ArrayList<String>> graph;
    public BookGraph(){
        graph = new HashMap<>();
    }    

    public void addBook(String isbn){
        if(!graph.containsKey(isbn)){
            graph.put(isbn, new ArrayList<>());
        }
    }
    public void addRelationship(String isbn1, String isbn2){
        addBook(isbn1);
        addBook(isbn2);

        graph.get(isbn1).add(isbn2);
        graph.get(isbn2).add(isbn1);
    }
    public void display(){
        for (String isbn : graph.keySet()){
            System.out.println(isbn + "->");
            for(String related : graph.get(isbn)){
                System.out.println(related + " ");
            }
            System.out.println();
        }
    }
    public void bfs(String start){
        if(!graph.containsKey(start)){
            System.out.println("Book not found in graph.");
            return;
        }
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while(!queue.isEmpty()){
            String current = queue.poll();
            System.out.println(current);
            for(String neighbour : graph.get(current)){
                if(!visited.contains(neighbour)){
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }
    }
    public void dfs(String start){
        if(!graph.containsKey(start)){
            System.out.println("Book not found in graph.");
            return;
        }
        Set<String> visited = new HashSet<>();
        dfsRecursive(start, visited);
    }
    private void dfsRecursive(String current, Set<String> visited){
        visited.add(current);
        System.out.println(current);
        for(String neighbour : graph.get(current)){
            if(!visited.contains(neighbour)){
                dfsRecursive(neighbour, visited);
            }
        }
    }
    public void dfsUsingStack(String start){
        if(!graph.containsKey(start)){
            System.out.println("Book not found in the graph.");
            return;
        }
        Stack<String> stack = new Stack<>();
        Set<String> visited = new HashSet<>();
        stack.push(start);
        while(!stack.isEmpty()){
            String current = stack.pop();
            if(visited.contains(current)){
                continue;
            }
            visited.add(current);
            System.out.println(current);
            for(String neighbour : graph.get(current)){
                if(!visited.contains(neighbour)){
                    stack.push(neighbour);
                }
            }
        }
    }
}
