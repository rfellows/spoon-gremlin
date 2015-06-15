/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package ninja.mattburgess.metaverse.gremlin;

import org.pentaho.di.core.Const;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;

public class History extends LinkedList<String> {

  String historyFile;

  public History() {

  }

  public History( String historyFile ) {
    this.historyFile = historyFile;

    try {
      String line;
      final BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( historyFile ) ) );
      while ( ( line = reader.readLine() ) != null ) {
        add( line );
      }
      reader.close();
    } catch ( Exception e ) {
      // Do nothing
    }

  }

  private int index = -1;

  @Override
  public boolean add( String s ) {
    if ( Const.isEmpty( s ) ) {
      return false;
    }

    boolean result = super.add( s );
    index = size() - 1;
    return result;
  }

  public String up() {
    return ( index >= 0 ) ? get( index-- ) : null;
  }

  public String down() {
    return ( index >= 0 && index < size() - 1 ) ? this.get( index++ ) : "";
  }

  public void save() {
    save( historyFile );
  }

  public void save( String file ) {
    if ( file != null ) {
      try {
        PrintWriter pw = new PrintWriter( new FileWriter( historyFile, false ) );
        for ( String line : this ) {
          pw.println( line );
        }
        pw.close();
      } catch ( Exception e ) {
        // Hey we tried, no big deal
      }
    }
  }
}
