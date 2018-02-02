package lapsePlus.views;

/*
* ColorDecoratingLabelProvider.java,version 2.8, 2010
*/


import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Color;

public class ColorDecoratingLabelProvider extends DecoratingLabelProvider
  implements IColorProvider
{
  public ColorDecoratingLabelProvider(ILabelProvider provider, ILabelDecorator decorator)
  {
    super(provider, decorator);
  }

  @Override
public Color getForeground(Object element) {
    ILabelProvider labelProvider = getLabelProvider();
    if ((labelProvider instanceof IColorProvider))
      return ((IColorProvider)labelProvider).getForeground(element);
    return null;
  }

  @Override
public Color getBackground(Object element) {
    ILabelProvider labelProvider = getLabelProvider();
    if ((labelProvider instanceof IColorProvider))
      return ((IColorProvider)labelProvider).getBackground(element);
    return null;
  }
}
