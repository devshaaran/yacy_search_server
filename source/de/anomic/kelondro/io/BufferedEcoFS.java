// kelondroBufferedEcoFS.java
// (C) 2008 by Michael Peter Christen; mc@yacy.net, Frankfurt a. M., Germany
// first published 14.01.2008 on http://yacy.net
//
// $LastChangedDate: 2006-04-02 22:40:07 +0200 (So, 02 Apr 2006) $
// $LastChangedRevision: 1986 $
// $LastChangedBy: orbiter $
//
// LICENSE
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package de.anomic.kelondro.io;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import de.anomic.kelondro.util.FileUtils;


/**
 * The kelondroBufferedEcoFS extends the IO reduction to EcoFS by providing a
 * write buffer to elements that are INSIDE the filed entries of the file
 * That means, each time, an entry is written to the end of the file, it is NOT buffered here,
 * but possibly buffered in the enclosed kelondroEcoFS
 */
public class BufferedEcoFS {

    private EcoFS efs;
    private final int maxEntries;
    private final TreeMap<Long, byte[]> buffer;
    
    public BufferedEcoFS(final EcoFS efs, final int maxEntries) {
        this.efs = efs;
        this.maxEntries = maxEntries;
        this.buffer = new TreeMap<Long, byte[]>();
    }

    private void flushBuffer() throws IOException {
        if (efs == null) return;
        final Iterator<Map.Entry<Long, byte[]>> i = buffer.entrySet().iterator();
        Map.Entry<Long, byte[]> entry;
        while (i.hasNext()) {
            entry = i.next();
            efs.put(entry.getKey().intValue(), entry.getValue(), 0);
        }
        buffer.clear();
    }
    
    public synchronized long size() throws IOException {
        return efs == null ? 0 : efs.size(); // stuck
    }
    
    public File filename() {
        return efs.filename();
    }

    public synchronized void close() {
        try {
            flushBuffer();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (efs != null) efs.close();
        efs = null;
    }

    protected synchronized void finalize() {
        if (this.efs != null) this.close();
    }
    
    public synchronized void get(final long index, final byte[] b, final int start) throws IOException {
        assert b.length - start >= efs.recordsize;
        if (index >= size()) throw new IndexOutOfBoundsException("kelondroBufferedEcoFS.get(" + index + ") outside bounds (" + this.size() + ")");
        final byte[] bb = buffer.get(Long.valueOf(index));
        if (bb == null) {
            efs.get(index, b, start);
        } else {
            System.arraycopy(bb, 0, b, start, efs.recordsize);
        }
    }

    public synchronized void put(final long index, final byte[] b, final int start) throws IOException {
        assert b.length - start >= efs.recordsize;
        final long s = size();
        if (index > s) throw new IndexOutOfBoundsException("kelondroBufferedEcoFS.put(" + index + ") outside bounds (" + this.size() + ")");
        if (index == s) {
            efs.add(b, start);
        } else {
            final byte[] bb = new byte[efs.recordsize];
            System.arraycopy(b, start, bb, 0, efs.recordsize);
            buffer.put(Long.valueOf(index), bb);
            if (buffer.size() > this.maxEntries) flushBuffer();
       }
    }
    
    public synchronized void add(final byte[] b, final int start) throws IOException {
        assert b.length - start >= efs.recordsize;
        // index == size() == efs.size();
        efs.add(b, start);
    }

    public synchronized void cleanLast(final byte[] b, final int start) throws IOException {
        assert b.length - start >= efs.recordsize;
        final byte[] bb = buffer.remove(Long.valueOf(size() - 1));
        if (bb == null) {
            efs.cleanLast(b, start); // stuck
        } else {
            System.arraycopy(bb, 0, b, start, efs.recordsize);
            efs.cleanLast();
        }
    }
    
    public synchronized void cleanLast() throws IOException {
        buffer.remove(Long.valueOf(size() - 1));
        efs.cleanLast();
    }
    
    public void deleteOnExit() {
        efs.deleteOnExit();
    }
    
    /**
     * main - writes some data and checks the tables size (with time measureing)
     * @param args
     */
    public static void main(final String[] args) {
        // open a file, add one entry and exit
        final File f = new File(args[0]);
        if (f.exists()) FileUtils.deletedelete(f);
        try {
            final EcoFS t = new EcoFS(f, 8);
            final byte[] b = new byte[8];
            t.add("01234567".getBytes(), 0);
            t.add("ABCDEFGH".getBytes(), 0);
            t.add("abcdefgh".getBytes(), 0);
            t.add("--------".getBytes(), 0);
            t.add("********".getBytes(), 0);
            for (int i = 0; i < 1000; i++) t.add("++++++++".getBytes(), 0);
            t.add("=======0".getBytes(), 0);
            t.add("=======1".getBytes(), 0);
            t.add("=======2".getBytes(), 0);
            t.cleanLast(b, 0);
            System.out.println(new String(b));
            t.cleanLast(b, 0);
            //t.clean(2, b, 0);
            System.out.println(new String(b));
            t.get(1, b, 0);
            System.out.println(new String(b));
            t.put(1, "AbCdEfGh".getBytes(), 0);
            t.get(1, b, 0);
            System.out.println(new String(b));
            t.get(3, b, 0);
            System.out.println(new String(b));
            t.get(4, b, 0);
            System.out.println(new String(b));
            System.out.println("size = " + t.size());
            //t.clean(t.size() - 2);
            t.cleanLast();
            final long start = System.currentTimeMillis();
            long c = 0;
            for (int i = 0; i < 100000; i++) {
                c = t.size();
            }
            System.out.println("size() needs " + ((System.currentTimeMillis() - start) / 100) + " nanoseconds");
            System.out.println("size = " + c);
            
            t.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
