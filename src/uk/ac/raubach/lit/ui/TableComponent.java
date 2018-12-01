/*
 * Copyright 2018 Sebastian Raubach <sebastian@raubach.co.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.raubach.lit.ui;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.*;

import uk.ac.raubach.lit.pojo.*;
import uk.ac.raubach.lit.util.*;

/**
 * @author Sebastian Raubach
 */
public class TableComponent extends Composite
{
	//make sure you dispose these buttons when viewer input changes
	private Map<String, Button> tableButtons = new HashMap<>();

	private Set<Image>  images = new HashSet<>();
	private Table       table;
	private TableViewer tableViewer;

	public TableComponent(Composite parent, int style)
	{
		super(parent, style);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
	}

	public void init(Config config)
	{
		for (Control control : this.getChildren())
		{
			if (control != null && !control.isDisposed())
				control.dispose();

			images.forEach(Image::dispose);
		}

		List<String> setups = config.getSetups();

		table = new Table(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);

		tableViewer = new TableViewer(table)
		{
			private void clearResources()
			{
				tableButtons.values().forEach(Button::dispose);
				tableButtons.clear();
				images.forEach(Image::dispose);
				images.clear();
			}

			@Override
			public void update(Object element, String[] properties)
			{
				super.update(element, properties);

				for (TableColumn c : getTable().getColumns())
					c.pack();
			}

			@Override
			public void refresh()
			{
				clearResources();
				super.refresh();
			}

			@Override
			public void refresh(boolean updateLabels)
			{
				clearResources();
				super.refresh(updateLabels);
			}

			@Override
			protected void unmapAllElements()
			{
				clearResources();
				super.unmapAllElements();
			}
		};

		TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return "";
			}

			@Override
			public Image getImage(Object element)
			{
				File file = new File(((Config.Program) element).getPath());

				// First, try to get the icon from the file system
				Icon icon = FileSystemView.getFileSystemView()
										  .getSystemIcon(file);

				// If there is an icon, use it
				if (icon != null)
				{
					ImageData data = IconToImage.convertToSWT(icon);

					Image image = new Image(null, data);
					images.add(image);

					return image;
				}
				else
				{
					return super.getImage(element);
				}
			}
		});

		column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setText("Program");
		column.setEditingSupport(new ProgramNameEditingSupport(tableViewer));
		column.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return ((Config.Program) element).getProgram();
			}
		});
		column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setText("Path");

		DialogCellEditor editor = new DialogCellEditor(tableViewer.getTable())
		{
			@Override
			protected Object openDialogBox(Control control)
			{
				FileDialog dialog = new FileDialog(getShell());
				return dialog.open();
			}
		};
		column.setEditingSupport(new AbstractEditingSupport(tableViewer, editor)
		{
			@Override
			protected void doSetValue(Object element, Object value)
			{
				((Config.Program) element).setPath(String.valueOf(value));
				tableViewer.update(element, null);
			}

			@Override
			protected Object getValue(Object o)
			{
				return ((Config.Program) o).getPath();
			}
		});

		column.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return ((Config.Program) element).getPath();
			}
		});

		for (String setup : setups)
		{
			final String s = setup;
			column = new TableViewerColumn(tableViewer, SWT.NONE);
			column.getColumn().setText(setup);
			column.setLabelProvider(new ColumnLabelProvider()
			{
				@Override
				public void update(ViewerCell cell)
				{
					final Config.Program project = (Config.Program) cell.getElement();

					TableItem item = (TableItem) cell.getItem();
					String key = project.getProgram() + "-" + s;
					Button button;
					if (tableButtons.containsKey(key))
					{
						button = tableButtons.get(key);
					}
					else
					{
						button = new Button((Composite) cell.getViewerRow().getControl(), SWT.CHECK);
						tableButtons.put(key, button);

						button.addListener(SWT.Selection, e -> {
							boolean selected = button.getSelection();

							if (selected && !project.getSetups().contains(s))
								project.getSetups().add(s);
							if (!selected && project.getSetups().contains(s))
								project.getSetups().remove(s);
						});
					}
					button.pack();
					TableEditor editor = new TableEditor(item.getParent());
					editor.minimumWidth = button.getSize().x;
					editor.horizontalAlignment = SWT.CENTER;
					editor.setEditor(button, item, cell.getColumnIndex());
					editor.layout();

					button.setSelection(project.getSetups().contains(s));
				}
			});
		}

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(config.getPrograms());

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tableViewer, new FocusCellOwnerDrawHighlighter(tableViewer));

		ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(tableViewer)
		{
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event)
			{
				if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION)
				{
					EventObject source = event.sourceEvent;
					if (source instanceof MouseEvent && ((MouseEvent) source).button == 3)
						return false;
				}
				return super.isEditorActivationEvent(event) || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR);
			}
		};

		TableViewerEditor.create(tableViewer, focusCellManager, activationSupport, ColumnViewerEditor.TABBING_HORIZONTAL |
			ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR |
			ColumnViewerEditor.TABBING_VERTICAL |
			ColumnViewerEditor.KEYBOARD_ACTIVATION);

		for (TableColumn c : tableViewer.getTable().getColumns())
			c.pack();

		Composite buttonComp = new Composite(this, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		GridLayout layout = new GridLayout(2, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonComp.setLayout(layout);

		Button add = new Button(buttonComp, SWT.NONE);
		add.setText("+");
		add.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		add.addListener(SWT.Selection, e -> {
			Config.Program program = new Config.Program();
			config.addProgram(program);
			tableViewer.refresh();
		});
		Button del = new Button(buttonComp, SWT.NONE);
		del.setText("-");
		del.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		del.addListener(SWT.Selection, e -> {
			IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();

			if (selection != null && selection.size() > 0)
			{
				Config.Program program = (Config.Program) selection.getFirstElement();
				config.removeProgram(program);
				tableViewer.refresh();
			}
		});

		getShell().layout(true, true);
	}

	@Override
	public void dispose()
	{
		super.dispose();
		images.forEach(Image::dispose);
	}

	public class ProgramNameEditingSupport extends EditingSupport
	{

		private final TableViewer viewer;
		private final CellEditor  editor;

		public ProgramNameEditingSupport(TableViewer viewer)
		{
			super(viewer);
			this.viewer = viewer;
			this.editor = new TextCellEditor(viewer.getTable());
		}

		@Override
		protected CellEditor getCellEditor(Object element)
		{
			return editor;
		}

		@Override
		protected boolean canEdit(Object element)
		{
			return true;
		}

		@Override
		protected Object getValue(Object element)
		{
			return ((Config.Program) element).getProgram();
		}

		@Override
		protected void setValue(Object element, Object userInputValue)
		{
			((Config.Program) element).setProgram(String.valueOf(userInputValue));
			viewer.update(element, null);
		}
	}

	protected abstract class AbstractEditingSupport extends EditingSupport
	{
		private CellEditor editor;

		public AbstractEditingSupport(TableViewer viewer, CellEditor anEditor)
		{
			super(viewer);
			this.editor = anEditor;
		}

		protected boolean canEdit(Object element)
		{
			return editor != null;
		}

		protected CellEditor getCellEditor(Object element)
		{
			return editor;
		}

		protected void setValue(Object element, Object value)
		{
			doSetValue(element, value);
			getViewer().update(element, null);
		}

		protected abstract void doSetValue(Object element, Object value);
	}
}
