public class MergeSort{
    public static void sort(Book[] books, int left, int right){
        if(left >= right){
            return;
        }
        int middle = left + (right - left) / 2;
        sort(books, left, middle);
        sort(books, middle + 1, right);
        merge(books, left, middle, right);
    }
    private static void merge(
            Book[] books,
            int left,
            int middle,
            int right){
        int leftSize = middle - left + 1;
        int rightsize = right - middle;
        Book[] leftArray = new Book[leftSize];
        Book[] rightArray = new Book[rightsize];
        for(int i = 0; i < leftSize; i++){
            leftArray[i] = books[left + i];
        }
        for(int i = 0; i < rightsize; i++){
            rightArray[i] = books[middle +1 +i];
        }
        int i = 0;
        int j = 0;
        int k = left;

        while (i < leftSize && j < rightsize){
            if(leftArray[i].getTitle().compareToIgnoreCase(rightArray[j].getTitle()) <= 0){
                books[k] = leftArray[i];
                i++;
            }else{
                books[k] = rightArray[j];
                j++;
            }
            k++;
        }
        while(i < leftSize){
            books[k] = leftArray[i];
            i++;
            j++;
        }
        while(j < rightsize){
            books[k] = rightArray[j];
            j++;
            k++;
        }
    }
}