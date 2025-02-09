/*
 * Copyright 2009-2020 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.hee.web.selection.frontend.plugin;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.extensions.markup.html.form.palette.theme.DefaultTheme;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.hippoecm.frontend.PluginRequestTarget;
import org.hippoecm.frontend.attributes.ClassAttribute;
import org.hippoecm.frontend.editor.ITemplateEngine;
import org.hippoecm.frontend.editor.plugins.field.FieldPluginHelper;
import org.hippoecm.frontend.editor.plugins.fieldhint.FieldHint;
import org.hippoecm.frontend.model.IModelReference;
import org.hippoecm.frontend.model.JcrItemModel;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.event.IObservable;
import org.hippoecm.frontend.model.event.IObserver;
import org.hippoecm.frontend.model.properties.JcrMultiPropertyValueModel;
import org.hippoecm.frontend.model.properties.JcrPropertyModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.standards.diff.LCS;
import org.hippoecm.frontend.plugins.standards.diff.LCS.Change;
import org.hippoecm.frontend.service.IEditor;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.frontend.types.IFieldDescriptor;
import org.hippoecm.frontend.validation.IValidationResult;
import org.hippoecm.frontend.validation.ValidatorUtils;
import org.hippoecm.frontend.validation.ViolationUtils;
import org.onehippo.forge.selection.frontend.model.ValueList;
import org.onehippo.forge.selection.frontend.plugin.Config;
import org.onehippo.forge.selection.frontend.plugin.DynamicMultiSelectPlugin;
import org.onehippo.forge.selection.frontend.plugin.sorting.SortHelper;
import org.onehippo.forge.selection.frontend.provider.IValueListNameProvider;
import org.onehippo.forge.selection.frontend.provider.IValueListProvider;
import org.onehippo.forge.selection.frontend.utils.SelectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hippoecm.frontend.validation.ViolationUtils.getFirstFieldViolation;

/**
 * <p>A {@link DynamicMultiSelectPlugin} with value-list path constructed based on the channel
 * in which the Editor creating the document.</p>
 *
 * <p>This class is essentially a copy of {@link DynamicMultiSelectPlugin} with logic to construct value-list path
 * by channel has been included.</p>
 *
 * <p>The logic to build value-list path based on channel has been provided
 * by the configured {@link IValueListNameProvider} via 'nameProvider' plugin config.</p>
 *
 * <p>Falls back to value-list provided by 'source' plugin config (if any) in case
 * if a {@link IValueListNameProvider} isn't configured.</p>
 */
public class ChannelBasedDynamicMultiSelectPlugin extends RenderPlugin {

    /**
     * Document path REGEX pattern
     */
    private static final Pattern DOCUMENT_PATH_REGEX_PATTERN = Pattern.compile("/content/documents/(.*?)/.*");

    private static final Logger log = LoggerFactory.getLogger(ChannelBasedDynamicMultiSelectPlugin.class);
    private static final CssResourceReference CSS = new CssResourceReference(ChannelBasedDynamicMultiSelectPlugin.class,
            "DynamicMultiSelectPlugin.css");
    private static final String CONFIG_TYPE = "multiselect.type";
    private static final String CONFIG_SELECT_MAX_ROWS = "selectlist.maxrows";
    private static final String CONFIG_CHECKBOXES = "checkboxes";
    private static final String CONFIG_PALETTE = "palette";
    private static final String CONFIG_PALETTE_MAX_ROWS = "palette.maxrows";
    private static final String CONFIG_PALETTE_ALLOW_ORDER = "palette.alloworder";
    private static final String CONFIG_VALUELIST_OPTIONS = "valuelist.options";
    private static final String CONFIG_CLUSTER_OPTIONS = "cluster.options";
    private static final long serialVersionUID = -2378511494430787874L;
    private final FieldPluginHelper helper;
    private final IEditor.Mode mode;

    private JcrPropertyModel propertyModel;
    private IObserver propertyObserver;

    public ChannelBasedDynamicMultiSelectPlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);

        mode = IEditor.Mode.fromString(config.getString(ITemplateEngine.MODE, "view"));
        helper = new FieldPluginHelper(context, config);
        final JcrMultiPropertyValueModel<String> model = new JcrMultiPropertyValueModel<>(
                getPropertyModel().getItemModel());

        subscribe();

        // use caption for backwards compatibility; i18n should use field name
        add(new Label("name", helper.getCaptionModel(this)));
        add(new FieldHint("hint-panel", helper.getHintModel(this)));

        // required
        final Label required = new Label("required", "*");
        if (helper.getField() == null || !ValidatorUtils.hasRequiredValidator(helper.getField().getValidators())) {
            required.setVisible(false);
        }
        add(required);

        // configured provider
        final IValueListProvider selectedProvider = context.getService(config.getString(IValueListProvider.SERVICE),
                IValueListProvider.class);

        if (selectedProvider == null) {
            log.warn("DynamicMultiSelectPlugin: value list provider cannot be found by name '{}'",
                    config.getString(IValueListProvider.SERVICE));

            // dummy markup
            final Fragment modeFragment = new Fragment("mode", "view", this);
            modeFragment.add(new ListView("viewitems", Collections.EMPTY_LIST, null));
            add(modeFragment);
            addListSelectionFragments(false/*visible*/, null, null);
            return;

        }

        // HIPPLUG-908: Start using cluster.options instead of valuelist.options, maintaining backwards compatibility.
        IPluginConfig options = config.getPluginConfig(CONFIG_CLUSTER_OPTIONS);
        if (options == null) {
            options = config.getPluginConfig(CONFIG_VALUELIST_OPTIONS);
            if (options == null) {
                throw new WicketRuntimeException("Configuration node '" + CONFIG_CLUSTER_OPTIONS
                        + "' not found in plugin configuration. " + config);
            }

            log.warn("The configuration node name '{}' is deprecated. Rename it to '{}'. options={}",
                    CONFIG_VALUELIST_OPTIONS, CONFIG_CLUSTER_OPTIONS, options);
        }

        // Get value-list by channel
        String valueListPath = getValueListByChannel();

        Node valueListNode;
        try {
            valueListNode = getSession().getJcrSession().getNode(valueListPath);
        } catch (final RepositoryException e) {
            log.debug("Can't retrieve the value-list '{}' node. " +
                            "It is possible that the value-list '{}' doesn't exists in the repository",
                    valueListPath, valueListPath);
            valueListNode = null;
        }

        if (StringUtils.isEmpty(valueListPath) || valueListNode == null) {
            // Falls back to 'source' value-list (if value-list by channel can't be constructed
            // or doesn't exists in the repository) in case if any provided
            valueListPath = options.getString(Config.SOURCE);
            log.warn("Falling back to value-list '{}' configured via 'source' plugin config (in case if any) " +
                    "as channel specific value-list is either can't be constructed or isn't available " +
                    "in the repository", valueListPath);
        }

        log.debug("Field '{}' will use value-list '{}' to populate items",
                helper.getField().getName(), valueListPath);

        final Locale locale = SelectionUtils.getLocale(SelectionUtils.getNode(model));
        final ValueList valueList = selectedProvider.getValueList(valueListPath, locale);

        new SortHelper().sort(valueList, options);

        final ListModel<String> choicesModel = new ListModel<>(valueList.stream()
                .map(org.onehippo.forge.selection.frontend.model.ListItem::getKey)
                .collect(Collectors.toCollection(ArrayList::new)));

        final Fragment modeFragment;
        final String mode = config.getString(ITemplateEngine.MODE);
        switch (mode) {
            case "edit":
                modeFragment = populateEditMode(config, model, valueList, choicesModel);
                break;
            case "compare":
                modeFragment = populateCompareMode(context, config, model, valueList);
                break;
            default:
                modeFragment = populateViewMode(model, valueList);
        }
        add(modeFragment);
    }

    /**
     * Returns value-list path by channel in which document is being created.
     *
     * @return the value-list path by channel in which document is being created.
     */
    private String getValueListByChannel() {
        final String channel = getChannel();

        if (StringUtils.isNotEmpty(channel)) {
            return getValueListByNameProvider(channel);
        }

        return StringUtils.EMPTY;
    }

    /**
     * <p>Returns channel in which the document is being created. Otherwise, returns an Empty String
     * if channel can't be extracted from the current document path.</p>
     *
     * <p>It extracts channel from the document path using {@code /content/documents/<channel>/.*}</p> pattern.</p>
     *
     * @return the channel in which the document is being created.
     * Otherwise, returns an Empty String if channel can't be extracted from the current document path.
     */
    private String getChannel() {
        try {
            final String documentNodePath = helper.getNodeModel().getNode().getPath();
            log.debug("Document node path = {}", documentNodePath);

            final Matcher documentPathMatcher = DOCUMENT_PATH_REGEX_PATTERN.matcher(documentNodePath);

            if (documentPathMatcher.find()) {
                final String channel = documentPathMatcher.group(1);
                log.debug("Document channel = {}", channel);

                return channel;
            }
        } catch (final RepositoryException e) {
            log.error("Caught error {} while retrieving current document node path", e.getMessage(), e);
        }

        return StringUtils.EMPTY;
    }

    /**
     * Returns value-list returned by the {@link IValueListNameProvider} configured
     * via 'nameProvider' plugin config (in case if any). Otherwise, returns an Empty String.
     *
     * @param channel the channel in which the document is being created.
     * @return value-list returned by the {@link IValueListNameProvider} configured
     * via 'nameProvider' plugin config (in case if any). Otherwise, returns an Empty String.
     */
    private String getValueListByNameProvider(final String channel) {
        IValueListNameProvider valueListNameProvider = null;

        final String nameProvider = getPluginConfig().getPluginConfig(CONFIG_CLUSTER_OPTIONS)
                .getString(Config.NAME_PROVIDER);
        log.debug("Plugin config 'nameProvider' = {}", nameProvider);

        if (StringUtils.isNotBlank(nameProvider)) {
            try {
                valueListNameProvider = (IValueListNameProvider) Class.forName(nameProvider.trim()).newInstance();
            } catch (final Exception e) {
                log.error("Cannot instantiate name provider class {}: {}", nameProvider, e.getMessage());
            }
        }

        if (valueListNameProvider == null) {
            return StringUtils.EMPTY;
        }

        final String valueListPath = valueListNameProvider.getValueListName(channel, getPluginConfig());
        log.debug("value-list provided by '{}' ValueListNameProvider: {}", nameProvider, valueListPath);

        return valueListPath;
    }

    @Override
    public void render(final PluginRequestTarget target) {
        if (isActive() && IEditor.Mode.EDIT == mode) {
            final IFieldDescriptor field = helper.getField();
            final IModel<IValidationResult> validationModel = helper.getValidationModel();

            getFirstFieldViolation(field, validationModel).ifPresent(violationMessage -> {
                final String selector = String.format("$('#%s')", getMarkupId());
                target.appendJavaScript(ViolationUtils.getFieldViolationScript(selector, violationMessage));
            });
        }

        super.render(target);
    }

    protected FieldPluginHelper getFieldHelper() {
        return helper;
    }

    /**
     * Checks if a field has any violations attached to it.
     *
     * @param validationResult The IValidationResult that contains all violations that occurred for this editor
     * @return true if there are no violations present or non of the validation belong to the current field
     * @deprecated This is handled by calling {@link ViolationUtils#getFirstFieldViolation} and checking if a violation
     * is present
     */
    @Deprecated
    protected boolean isFieldValid(final IValidationResult validationResult) {
        final IFieldDescriptor field = helper.getField();
        final Optional<ViolationUtils.ViolationMessage> violation = getFirstFieldViolation(field,
                Model.of(validationResult));
        return !violation.isPresent() && isContainerValid();
    }

    private boolean isContainerValid() {
        final IFeedbackMessageFilter filter = new ContainerFeedbackMessageFilter(this);
        return !getSession().getFeedbackMessages().hasMessage(filter);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(CSS));
    }

    @Override
    protected void onDetach() {
        if (this.propertyModel != null) {
            this.propertyModel.detach();
        }
        helper.detach();
        super.onDetach();
    }

    @Override
    public void onModelChanged() {
        unsubscribe();
        subscribe();
    }

    protected Fragment populateViewMode(final JcrMultiPropertyValueModel<String> model, final ValueList valueList) {
        final Fragment modeFragment = new Fragment("mode", "view", this);// show view list
        modeFragment.add(new ListView("viewitems", model.getObject(), valueList));
        addListSelectionFragments(false/*visible*/, null, model);

        return modeFragment;
    }

    protected Fragment populateCompareMode(final IPluginContext context, final IPluginConfig config,
                                           final JcrMultiPropertyValueModel<String> model, final ValueList valueList) {
        final Fragment modeFragment = new Fragment("mode", "view", this);

        final IModelReference compareToRef = context.getService(config.getString("model.compareTo"),
                IModelReference.class);
        if (compareToRef != null) {
            final JcrNodeModel baseNodeModel = (JcrNodeModel) compareToRef.getModel();
            if (baseNodeModel != null && baseNodeModel.getNode() != null) {
                final IFieldDescriptor field = helper.getField();
                try {
                    if (baseNodeModel.getNode().hasProperty(field.getPath())) {
                        final JcrMultiPropertyValueModel<String> baseModel = new JcrMultiPropertyValueModel<>(
                                new JcrItemModel<>(baseNodeModel.getNode().getProperty(field.getPath()))
                        );

                        final List<String> baseOptions = baseModel.getObject();
                        final List<String> currentOptions = model.getObject();
                        final List<Change<String>> changes = LCS.getChangeSet(
                                baseOptions.toArray(new String[0]),
                                currentOptions.toArray(new String[0])
                        );
                        // show view list
                        modeFragment.add(new CompareView("viewitems", changes, valueList));
                    } else {
                        modeFragment.add(new ListView("viewitems", model.getObject(), valueList));
                    }
                } catch (final RepositoryException e) {
                    log.error("RepositoryException : ", e);
                }

            } else {
                modeFragment.add(new ListView("viewitems", model.getObject(), valueList));
            }
        } else {
            modeFragment.add(new ListView("viewitems", model.getObject(), valueList));
        }

        // hide dummy fragment
        addListSelectionFragments(false, null, model);
        return modeFragment;
    }

    protected Fragment populateEditMode(final IPluginConfig config, final JcrMultiPropertyValueModel<String> model,
                                        final ValueList valueList, final ListModel<String> choicesModel) {
        final Fragment modeFragment = new Fragment("mode", "edit", this);

        final Fragment typeFragment;
        final String type = config.getString(CONFIG_TYPE);
        if (CONFIG_CHECKBOXES.equals(type)) {
            typeFragment = addCheckboxes(model, valueList, choicesModel);
        } else if (CONFIG_PALETTE.equals(type)) {
            typeFragment = addPalette(config, model, valueList, choicesModel);
        } else {
            typeFragment = addList(config, model, valueList, choicesModel);
        }
        modeFragment.add(typeFragment);
        return modeFragment;
    }

    protected Fragment addList(final IPluginConfig config, final JcrMultiPropertyValueModel<String> model,
                               final ValueList valueList, final ListModel<String> choicesModel) {
        final Fragment typeFragment = new Fragment("type", "edit-select", this);

        final ListMultipleChoice<String> multiselect = new ListMultipleChoice<>("multiselect", model, choicesModel,
                new ValueListItemRenderer(valueList));

        // trigger setObject on selection changed
        multiselect.add(new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = -1883481862983622070L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
            }
        });

        // set (configured) max rows
        final String maxRows = config.getString(CONFIG_SELECT_MAX_ROWS, "8");
        try {
            multiselect.setMaxRows(Integer.parseInt(maxRows));
        } catch (final NumberFormatException nfe) {
            log.warn("The configured value '{}' for {} is not a valid number. Defaulting to 8.", maxRows,
                    CONFIG_SELECT_MAX_ROWS);
            multiselect.setMaxRows(8);
        }

        typeFragment.add(multiselect);
        addListSelectionFragments(true/*visible*/, multiselect, model);
        return typeFragment;
    }

    protected void addListSelectionFragments(final boolean visible, final ListMultipleChoice<String> multiselect, final JcrMultiPropertyValueModel<String> model) {
        final Fragment fragment = new Fragment("selectlinks", "edit-selectlinks", this);

        fragment.add(new SelectLink("select-link", multiselect, model));
        fragment.add(new UnselectLink("unselect-link", multiselect, model));
        fragment.setVisibilityAllowed(visible);

        add(fragment);
    }


    protected Fragment addPalette(final IPluginConfig config, final JcrMultiPropertyValueModel<String> model,
                                  final ValueList valueList, final ListModel<String> choicesModel) {
        final Fragment typeFragment = new Fragment("type", "edit-palette", this);

        // set (configured) max rows
        int rows = 10;
        final String maxRows = config.getString(CONFIG_PALETTE_MAX_ROWS, "10");
        try {
            rows = Integer.parseInt(maxRows);
        } catch (final NumberFormatException nfe) {
            log.warn("The configured value '{}' for {} is not a valid number. Defaulting to 10.", maxRows,
                    CONFIG_PALETTE_MAX_ROWS);
        }

        // set (configured) allow order value
        final boolean allowOrder = config.getBoolean(CONFIG_PALETTE_ALLOW_ORDER);

        final Palette<String> palette = new Palette<String>("palette", model, choicesModel,
                new ValueListItemRenderer(valueList), rows, allowOrder) {

            private static final long serialVersionUID = -597514873993912518L;

            // FIXME: workaround for WICKET-2843
            @Override
            public Collection<String> getModelCollection() {
                return new ArrayList<>(super.getModelCollection());
            }

            // trigger setObject on selection changed
            @Override
            protected Recorder<String> newRecorderComponent() {
                final Recorder<String> recorder = super.newRecorderComponent();
                recorder.add(new AjaxFormComponentUpdatingBehavior("change") {
                    private static final long serialVersionUID = -3870799703545526594L;

                    @Override
                    protected void onUpdate(final AjaxRequestTarget target) {
                        final Iterator<String> selectedChoices = recorder.getPalette().getSelectedChoices();
                        model.setObject(Lists.newArrayList(selectedChoices));
                    }
                });
                return recorder;
            }
        };
        palette.add(new DefaultTheme());
        typeFragment.add(palette);

        // hide fragments for list view
        addListSelectionFragments(false/*visible*/, null, model);

        return typeFragment;
    }

    protected Fragment addCheckboxes(final JcrMultiPropertyValueModel<String> model, final ValueList valueList,
                                     final ListModel<String> choicesModel) {
        final Fragment typeFragment = new Fragment("type", "edit-checkboxes", this);

        final CheckBoxMultipleChoice<String> checkboxes = new CheckBoxMultipleChoice<>("checkboxes", model,
                choicesModel,
                new ValueListItemRenderer(valueList));
        checkboxes.setPrefix("<div class=\"checkbox-container\">");
        checkboxes.setSuffix("</div>");

        // trigger setObject on selection changed
        checkboxes.add(new AjaxFormChoiceComponentUpdatingBehavior() {
            private static final long serialVersionUID = 4095333387656663756L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
            }
        });

        typeFragment.add(checkboxes);

        // hide fragments for list view
        addListSelectionFragments(false/*visible*/, null, model);

        return typeFragment;
    }

    protected JcrPropertyModel getPropertyModel() {
        return new JcrPropertyModel(helper.getFieldItemModel());
    }

    /**
     * Subscribe to a service to get notified of property changes.
     */
    protected void subscribe() {
        propertyModel = getPropertyModel();
        if (propertyModel != null) {

            getPluginContext().registerService(propertyObserver = new IObserver() {

                private static final long serialVersionUID = 1152585463393809486L;

                @Override
                public IObservable getObservable() {
                    return propertyModel;
                }

                @Override
                public void onEvent(final Iterator events) {
                    redraw();
                }

            }, IObserver.class.getName());
        }
    }

    /**
     * Unsubscribe from the change notification service.
     */
    protected void unsubscribe() {
        if (propertyModel != null) {
            getPluginContext().unregisterService(propertyObserver, IObserver.class.getName());
            propertyModel = null;
        }
    }

    protected static class ValueListItemRenderer implements IChoiceRenderer<String> {

        private static final long serialVersionUID = 528588135620413905L;
        private final ValueList valueList;

        public ValueListItemRenderer(final ValueList valueList) {
            this.valueList = valueList;
        }

        @Override
        public String getDisplayValue(final String object) {
            return valueList.getLabel(object);
        }

        @Override
        public String getIdValue(final String object, final int index) {
            return object;
        }

        @Override
        public String getObject(final String id, final IModel<? extends List<? extends String>> choicesModel) {
            final List<? extends String> choices = choicesModel.getObject();
            return choices.contains(id) ? id : null;
        }
    }

    /**
     * Repeating view to show items in view mode.
     */
    protected class ListView extends RefreshingView<String> {

        private static final long serialVersionUID = 3304409309604286453L;
        private final Collection<IModel<String>> models = new ArrayList<>();

        public ListView(final String id, final Collection<?> actualValues, final ValueList choices) {
            super(id);

            // get the choice labels by the actual values/keys
            for (final Object item : actualValues) {
                models.add(Model.of(choices.getLabel(item)));
            }
        }

        @Override
        protected Iterator<IModel<String>> getItemModels() {
            return models.iterator();
        }

        @Override
        protected void populateItem(final Item<String> item) {
            item.add(new Label("viewitem", item.getModelObject()));
        }
    }

    /**
     * Repeating view to show items in compare mode.
     */
    protected class CompareView extends org.apache.wicket.markup.html.list.ListView<Change<String>> {

        private static final long serialVersionUID = 7400940220549376028L;
        private final ValueList choices;

        public CompareView(final String id, final List<Change<String>> changes, final ValueList choices) {
            super(id, changes);
            this.choices = choices;
        }

        @Override
        protected void populateItem(final ListItem<Change<String>> item) {
            final Change<String> change = item.getModelObject();

            final Label label = new Label("viewitem", choices.getLabel(change.getValue()));
            switch (change.getType()) {
                case ADDED:
                    label.add(ClassAttribute.append("hippo-diff-added"));
                    break;
                case REMOVED:
                    label.add(ClassAttribute.append("hippo-diff-removed"));
                    break;
            }
            item.add(label);
        }
    }

    // TODO: UnselectLink and SelectLink should be combined and declared as static private classes.
    // It should be done in the next major release, i.e., 5.x.

    /**
     * Link unselect all values from a select list.
     */
    protected class UnselectLink extends AjaxLink<List<String>> {

        private static final long serialVersionUID = 2654883467046793259L;
        private final ListMultipleChoice multiselect;

        UnselectLink(final String id, final ListMultipleChoice multiselect, final IModel<List<String>> model) {
            super(id, model);
            this.multiselect = multiselect;
        }

        @Override
        public void onClick(final AjaxRequestTarget target) {

            // clear model
            setModelObject(null);

            // make the multiselect update to remove selected items
            target.add(multiselect);
        }
    }

    /**
     * Link select all values from a select list.
     */
    protected class SelectLink extends AjaxLink<List<String>> {

        private static final long serialVersionUID = 1194414574271991183L;
        private final ListMultipleChoice multiselect;

        SelectLink(final String id, final ListMultipleChoice multiselect, final IModel<List<String>> model) {
            super(id, model);
            this.multiselect = multiselect;
        }

        @Override
        public void onClick(final AjaxRequestTarget target) {

            // select all options
            setModelObject(multiselect.getChoices());

            // make the multiselect update to remove selected items
            target.add(multiselect);
        }
    }
}