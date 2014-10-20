package CacheTree;

/**
 * Created by valchu on 20/10/14.
 */
public class Lock {
    private int readers = 0;
    private boolean writting = false;

    public Lock () { }

    public synchronized void read () {
        while (writting) Thread.yield();
        readers++;
    }

    public synchronized void unread() {
        readers--;
    }

    public synchronized void write () {
        while (readers > 0) Thread.yield();
        writting = true;
    }

    public synchronized void unwrite () {
        writting = false;
    }
}
