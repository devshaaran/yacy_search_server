// servletProperties.java
// -------------------------------
// (C) 2006 Alexander Schier
// part of YaCy
//
// last major change: 06.02.2006
// this file is contributed by Alexander Schier
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
// Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  US
package de.anomic.server;

import net.yacy.cora.protocol.ResponseHeader;

public class servletProperties extends serverObjects {

    private static final long serialVersionUID = 1L;
    
    public static final String PEER_STAT_VERSION = "version";
    public static final String PEER_STAT_UPTIME = "uptime";
    public static final String PEER_STAT_MYTIME = "mytime";
    public static final String PEER_STAT_CLIENTNAME = "clientname";
    public static final String PEER_STAT_CLIENTID = "clientid";

    private String prefix="";
    
    private  ResponseHeader outgoingHeader;
    
    public servletProperties(){
        super();
    }
    
    public servletProperties(final serverObjects so) {
        super(so);
    }
    
    public void setOutgoingHeader(final ResponseHeader outgoingHeader) {
        this.outgoingHeader = outgoingHeader;
    }
    
    public ResponseHeader getOutgoingHeader() {
        if(outgoingHeader == null)
            return new ResponseHeader();
        return outgoingHeader;
    }
    
    public void setPrefix(final String myprefix) {
        prefix=myprefix;
    }
    
    public String put(final String key, final byte[] value) {
        return super.put(prefix + key, value);
    }
    
    public long put(final String key, final long value) {
        return super.put(prefix + key, value);
    }
    
    public long inc(final String key) {
        return super.inc(prefix+key);
    }
    
    public Object get(final String key, final Object dflt) {
        return super.get(prefix+key, dflt);
    }
    
    public String get(final String key, final String dflt) {
        return super.get(prefix+key, dflt);
    }
    
    public int getInt(final String key, final int dflt) {
        return super.getInt(prefix+key, dflt);
    }
    
    public long getLong(final String key, final long dflt) {
        return super.getLong(prefix+key, dflt);
    }
}
