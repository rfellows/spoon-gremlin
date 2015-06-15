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


import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.ISpoonMenuController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import java.util.Enumeration;
import java.util.ResourceBundle;

@SpoonPlugin( id = "GremlinSpoonPlugin", image = "" )
@SpoonPluginCategories( { "spoon", "trans-graph" } )
public class GremlinSpoonPlugin extends AbstractXulEventHandler implements SpoonPluginInterface, ISpoonMenuController, SpoonLifecycleListener {

  ResourceBundle bundle = new ResourceBundle() {
    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject( String key ) {
      return BaseMessages.getString( GremlinSpoonPlugin.class, key );
    }
  };

  public GremlinSpoonPlugin() {

  }

  public String getName() {
    return "gremlinSpoonPlugin"; //$NON-NLS-1$
  }

  @Override
  public void onEvent( SpoonLifeCycleEvent evt ) {
    if ( evt.equals( SpoonLifeCycleEvent.STARTUP ) ) {
      Spoon spoon = ( (Spoon) SpoonFactory.getInstance() );
      spoon.addSpoonMenuController( this );
    }
  }

  @Override
  public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    ClassLoader cl = getClass().getClassLoader();
    container.registerClassLoader( cl );
    if ( category.equals( "spoon" ) || category.equals( "trans-graph" ) ) {
      container.loadOverlay( "spoon_overlays.xul", bundle );
      container.addEventHandler( this );
    }
  }

  @Override
  public SpoonLifecycleListener getLifecycleListener() {
    return this;
  }

  @Override
  public SpoonPerspective getPerspective() {
    return null;
  }

  @Override
  public void updateMenu( Document doc ) {
    // Empty method
  }

  public void showGremlinConsole() {
    final Spoon spoon = Spoon.getInstance();
    try {
      GremlinConsoleDialog gcm = new GremlinConsoleDialog( spoon.getShell() );
      gcm.open();
    } catch ( Exception e ) {
      showErrorDialog( e, "Error with Progress Monitor Dialog", "Error with Progress Monitor Dialog" );
    }
  }

  public void init() {

  }

  /**
   * Show an error dialog
   *
   * @param e       The exception to display
   * @param title   The dialog title
   * @param message The message to display
   */
  private void showErrorDialog( Exception e, String title, String message ) {
    new ErrorDialog( Spoon.getInstance().getShell(), title, message, e );
  }
}
