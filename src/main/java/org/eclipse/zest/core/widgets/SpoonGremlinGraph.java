package org.eclipse.zest.core.widgets;

import org.eclipse.swt.widgets.Composite;

/**
 * Created by mburgess on 5/20/15.
 */
public class SpoonGremlinGraph extends Graph {
  public SpoonGremlinGraph( Composite parent, int style ) {
    super( parent, style );
    this.style |= ZestStyles.IGNORE_INVISIBLE_LAYOUT;
  }
}
