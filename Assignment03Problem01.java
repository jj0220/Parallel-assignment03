import java.util.concurrent.locks.*;

class Node {
    int giftTag;
    Node next;
    
    Node(int tag) {
        giftTag = tag;
        next = null;
    }
}

class ConcurrentLinkedList {
    private Node head;
    private final ReentrantLock lock;
    
    ConcurrentLinkedList() {
        head = null;
        lock = new ReentrantLock();
    }
    
    void addGift(int giftTag) {
        Node newNode = new Node(giftTag);
        lock.lock();

        try {
            if (head == null || giftTag < head.giftTag) {
                newNode.next = head;
                head = newNode;
            } 
            else {
                Node current = head;

                while (current.next != null && current.next.giftTag < giftTag) {
                    current = current.next;
                }

                newNode.next = current.next;
                current.next = newNode;
            }
        } 
        finally {
            lock.unlock();
        }
    }
    
}

class Servant extends Thread {
    private final ConcurrentLinkedList linkedList;
    private final int[] giftsToAdd;
    
    Servant(ConcurrentLinkedList list, int[] add) {
        linkedList = list;
        giftsToAdd = add;
    }
    
    @Override
    public void run() {
        for (int gift : giftsToAdd) {
            linkedList.addGift(gift);
            System.out.println("Added and wrote thank you card to gift number: " + gift);
        }
    }
}

public class Assignment03Problem01 {
    public static void main(String[] args) {
        ConcurrentLinkedList linkedList = new ConcurrentLinkedList();
        int[] presents = new int[500000];

        for (int i = 0; i < presents.length; i++) {
            presents[i] = i + 1;
        }
        
        int presentsPerServant = presents.length / 4;
        Servant[] servants = new Servant[4];

        for (int i = 0; i < 4; i++) {
            int[] giftsToAdd = new int[presentsPerServant];

            System.arraycopy(presents, i * presentsPerServant, giftsToAdd, 0, presentsPerServant);

            servants[i] = new Servant(linkedList, giftsToAdd);
            servants[i].start();
        }
        
        for (Servant servant : servants) {
            try {
                servant.join();
            } 
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("All presents have been added to the chain of ordered presents and thank you notes have been sent");
    }
}
