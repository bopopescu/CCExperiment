package org.apache.batik.css.engine;
public class StringIntMap {
    protected Entry[] table;
    protected int count;
    public StringIntMap(int c) {
        table = new Entry[( c - ( c >> 2)) + 1];
    }
    public int get( String key ) {
        int hash = key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        for ( Entry e = table[ index ]; e != null; e = e.next ) {
            if ( ( e.hash == hash ) && e.key.equals( key ) ) {
                return e.value;
            }
        }
        return -1;
    }
    public void put( String key, int value ) {
        int hash = key.hashCode() & 0x7FFFFFFF;
        int index = hash % table.length;
        for ( Entry e = table[ index ]; e != null; e = e.next ) {
            if ( ( e.hash == hash ) && e.key.equals( key ) ) {
                e.value = value;
                return;
            }
        }
        int len = table.length;
        if ( count++ >= ( len - ( len >> 2 ) ) ) {
            rehash();
            index = hash % table.length;
        }
        Entry e = new Entry( hash, key, value, table[ index ] );
        table[ index ] = e;
    }
    protected void rehash() {
        Entry[] oldTable = table;
        table = new Entry[oldTable.length * 2 + 1];
        for ( int i = oldTable.length - 1; i >= 0; i-- ) {
            for ( Entry old = oldTable[ i ]; old != null; ) {
                Entry e = old;
                old = old.next;
                int index = e.hash % table.length;
                e.next = table[ index ];
                table[ index ] = e;
            }
        }
    }
    protected static class Entry {
        public final int hash;
        public String key;
        public int value;
        public Entry next;
        public Entry( int hash, String key, int value, Entry next ) {
            this.hash  = hash;
            this.key   = key;
            this.value = value;
            this.next  = next;
        }
    }
}
