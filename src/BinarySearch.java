public class BinarySearch {
    public static Book search(Book[] books,String target){
        int left = 0;
        int right = books.length -1;

        while(left <= right){
            int middle = left + (right - left) / 2;
            int comparison = books[middle].getTitle().compareToIgnoreCase(target);
        
        if(comparison == 0){
            return books[middle];
        }
        if(comparison < 0){
            left = middle + 1;
        }else{
            right = middle -1;
        }
    }
    return null;
    }
}
