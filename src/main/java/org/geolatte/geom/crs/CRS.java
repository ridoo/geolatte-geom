/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.crs;

import org.geolatte.geom.codec.CRSWKTDecoder;
import org.geolatte.geom.codec.WKTParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class CRS {

    private static Logger LOGGER = LoggerFactory.getLogger(CRS.class);
    private static Map<Integer, CoordinateReferenceSystem> crsMap = new HashMap<Integer, CoordinateReferenceSystem>(4000);
    private static final String DELIM = "\\|";

    static {
        try {
            loadCRS();
        } catch (IOException e) {
            new RuntimeException("Can't read spatial ref system definitions.");
        }
    }

    private static void loadCRS() throws IOException {
        BufferedReader reader = createReader();
        try {
            String line = reader.readLine();
            CRSWKTDecoder decoder = new CRSWKTDecoder();
            while (line != null) {
                addDefinition(line, decoder);
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
    }

    private static BufferedReader createReader() {
        InputStream in = CRS.class.getClassLoader().getResourceAsStream("spatial_ref_sys.txt");
        if (in == null) {
            throw new IllegalStateException("Can't find spatial_ref_sys definitions.");
        }
        return new BufferedReader(new InputStreamReader(in));
    }

    private static void addDefinition(String line, CRSWKTDecoder decoder) {
        String[] tokens = line.split(DELIM);
        if (!"EPSG".equals(tokens[0])) return;
        Integer srid = Integer.valueOf(tokens[1]);
        try {
            CoordinateReferenceSystem crs = decoder.decode(tokens[2]);
            crsMap.put(srid, crs);
        } catch (WKTParseException e) {
            LOGGER.warn(String.format("Can't parse srid %d (%s). \n%s", srid,tokens[2], e.getMessage()));
        }

    }

    public static CoordinateReferenceSystem create(int SRID) {
        return crsMap.get(SRID);
    }
}
