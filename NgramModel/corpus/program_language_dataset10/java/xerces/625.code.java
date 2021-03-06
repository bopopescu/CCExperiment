package org.apache.xerces.util;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
public class SoftReferenceSymbolTable extends SymbolTable {
    protected SREntry[] fBuckets = null;
    private final ReferenceQueue fReferenceQueue;
    public SoftReferenceSymbolTable(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        }
        if (initialCapacity == 0) {
            initialCapacity = 1;
        }
        fLoadFactor = loadFactor;
        fTableSize = initialCapacity;
        fBuckets = new SREntry[fTableSize];
        fThreshold = (int)(fTableSize * loadFactor);
        fCount = 0;
        fReferenceQueue = new ReferenceQueue();
    }
    public SoftReferenceSymbolTable(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }
    public SoftReferenceSymbolTable() {
        this(TABLE_SIZE, 0.75f);
    }
    public String addSymbol(String symbol) {
        clean();
        int bucket = hash(symbol) % fTableSize;
        for (SREntry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            SREntryData data = (SREntryData)entry.get();
            if (data == null) {
                continue;
            }
            if (data.symbol.equals(symbol)) {
                return data.symbol;
            }
        }
        if (fCount >= fThreshold) {
            rehash();
            bucket = hash(symbol) % fTableSize;
        } 
        symbol = symbol.intern();
        SREntry entry = new SREntry(symbol, fBuckets[bucket], bucket, fReferenceQueue);
        fBuckets[bucket] = entry;
        ++fCount;
        return symbol;
    } 
    public String addSymbol(char[] buffer, int offset, int length) {
        clean();
        int bucket = hash(buffer, offset, length) % fTableSize;
        OUTER: for (SREntry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            SREntryData data = (SREntryData)entry.get();
            if (data == null) {
                continue;
            }
            if (length == data.characters.length) {
                for (int i = 0; i < length; i++) {
                    if (buffer[offset + i] != data.characters[i]) {
                        continue OUTER;
                    }
                }
                return data.symbol;
            }
        }
        if (fCount >= fThreshold) {
            rehash();
            bucket = hash(buffer, offset, length) % fTableSize;
        } 
        String symbol = new String(buffer, offset, length).intern();
        SREntry entry = new SREntry(symbol, buffer, offset, length, fBuckets[bucket], bucket, fReferenceQueue);
        fBuckets[bucket] = entry;
        ++fCount;
        return symbol;
    } 
    protected void rehash() {
        int oldCapacity = fBuckets.length;
        SREntry[] oldTable = fBuckets;
        int newCapacity = oldCapacity * 2 + 1;
        SREntry[] newTable = new SREntry[newCapacity];
        fThreshold = (int)(newCapacity * fLoadFactor);
        fBuckets = newTable;
        fTableSize = fBuckets.length;
        for (int i = oldCapacity ; i-- > 0 ;) {
            for (SREntry old = oldTable[i] ; old != null ; ) {
                SREntry e = old;
                old = old.next;
                SREntryData data = (SREntryData)e.get();
                if (data != null) {
                    int index = hash(data.characters, 0, data.characters.length) % newCapacity;
                    if (newTable[index] != null) {
                        newTable[index].prev = e;
                    }
                    e.next = newTable[index];
                    e.prev = null;
                    newTable[index] = e;
                }
                else {
                    fCount--;
                }
            }
        }
    }
    public boolean containsSymbol(String symbol) {
        int bucket = hash(symbol) % fTableSize;
        int length = symbol.length();
        OUTER: for (SREntry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            SREntryData data = (SREntryData)entry.get();
            if (data == null) {
                continue;
            }
            if (length == data.characters.length) {
                for (int i = 0; i < length; i++) {
                    if (symbol.charAt(i) != data.characters[i]) {
                        continue OUTER;
                    }
                }
                return true;
            }
        }
        return false;
    } 
    public boolean containsSymbol(char[] buffer, int offset, int length) {
        int bucket = hash(buffer, offset, length) % fTableSize;
        OUTER: for (SREntry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            SREntryData data = (SREntryData)entry.get();
            if (data == null) {
                continue;
            }
            if (length == data.characters.length) {
                for (int i = 0; i < length; i++) {
                    if (buffer[offset + i] != data.characters[i]) {
                        continue OUTER;
                    }
                }
                return true;
            }
        }
        return false;
    } 
    private void removeEntry(SREntry entry) {
        if (entry.next != null) {
            entry.next.prev = entry.prev;
        }
        if (entry.prev != null) {
            entry.prev.next = entry.next;
        }
        else {
            fBuckets[entry.bucket] = entry.next;
        }
        fCount--;
    }
    private void clean() {
        SREntry entry = (SREntry)fReferenceQueue.poll();
        while (entry != null) {
            removeEntry(entry);
            entry = (SREntry)fReferenceQueue.poll();
        }
    }
    protected static final class SREntry extends SoftReference {
        public SREntry next;
        public SREntry prev;
        public int bucket;
        public SREntry(String internedSymbol, SREntry next, int bucket, ReferenceQueue q) {
            super(new SREntryData(internedSymbol), q);
            initialize(next, bucket);
        }
        public SREntry(String internedSymbol, char[] ch, int offset, int length, SREntry next, int bucket, ReferenceQueue q) {
            super(new SREntryData(internedSymbol, ch, offset, length), q);
            initialize(next, bucket);
        }
        private void initialize(SREntry next, int bucket) {
            this.next = next;
            if (next != null) {
                next.prev = this;
            }
            this.prev = null;
            this.bucket = bucket;
        }
    } 
    protected static final class SREntryData {
        public final String symbol;
        public final char[] characters;
        public SREntryData(String internedSymbol) {
            this.symbol = internedSymbol;
            characters = new char[symbol.length()];
            symbol.getChars(0, characters.length, characters, 0);
        }
        public SREntryData(String internedSymbol, char[] ch, int offset, int length) {
            this.symbol = internedSymbol;
            characters = new char[length];
            System.arraycopy(ch, offset, characters, 0, length);
        }
    }
} 
