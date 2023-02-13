package com.example.application.views.members;


import com.example.application.views.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;

@PageTitle("MembersList")
@Route(value = "membersList", layout = MainLayout.class)
@RolesAllowed({"ADMIN",})
public class MembersListView extends Div {

    private GridPro<MembersList> grid;
    private GridListDataView<MembersList> gridListDataView;

    private Grid.Column<MembersList> MembersListColumn;
    private Grid.Column<MembersList> amountColumn;
    private Grid.Column<MembersList> statusColumn;
    private Grid.Column<MembersList> dateColumn;

    public MembersListView() {
        addClassName("members-list-view");
        setSizeFull();
        createGrid();
        add(grid);
    }

    private void createGrid() {
        createGridComponent();
        addColumnsToGrid();
        addFiltersToGrid();
    }

    private void createGridComponent() {
        grid = new GridPro<>();
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("100%");

        List<MembersList> membersLists = getMembers();
        gridListDataView = grid.setItems(membersLists);
    }

    private void addColumnsToGrid() {
        createMembersListColumn();
        createAmountColumn();
        createStatusColumn();
        createDateColumn();
    }

    private void createMembersListColumn() {

        MembersListColumn = grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(Alignment.CENTER);
            Image img = new Image(client.getImg(), "");
            Span span = new Span();
            span.setClassName("name");
            span.setText(client.getMembers());
            hl.add(img, span);
            return hl;
        })).setComparator(client -> client.getMembers()).setHeader("Client");
    }

    private void createAmountColumn() {
        amountColumn = grid
                .addEditColumn(MembersList::getAmount,
                        new NumberRenderer<>(membersList -> membersList.getAmount(), NumberFormat.getCurrencyInstance(Locale.US)))
                .text((item, newValue) -> item.setAmount(Double.parseDouble(newValue)))
                .setComparator(membersList -> membersList.getAmount()).setHeader("Amount");
    }

    private void createStatusColumn() {
        statusColumn = grid.addEditColumn(MembersList::getMembers, new ComponentRenderer<>(client -> {
                    Span span = new Span();
                    span.setText(client.getStatus());
                    span.getElement().setAttribute("theme", "badge " + client.getStatus().toLowerCase());
                    return span;
                })).select((item, newValue) -> item.setStatus(newValue), Arrays.asList("Pending", "Success", "Error"))
                .setComparator(client -> client.getStatus()).setHeader("Status");
    }

    private void createDateColumn() {
        dateColumn = grid
                .addColumn(new LocalDateRenderer<>(membersList -> LocalDate.parse(membersList.getDate()),
                        () -> DateTimeFormatter.ofPattern("M/d/yyyy")))
                .setComparator(membersList -> membersList.getDate()).setHeader("Date").setWidth("180px").setFlexGrow(0);
    }

    private void addFiltersToGrid() {
        HeaderRow filterRow = grid.appendHeaderRow();

        TextField clientFilter = new TextField();
        clientFilter.setPlaceholder("Filter");
        clientFilter.setClearButtonVisible(true);
        clientFilter.setWidth("100%");
        clientFilter.setValueChangeMode(ValueChangeMode.EAGER);
        clientFilter.addValueChangeListener(event -> gridListDataView
                .addFilter(membersList -> StringUtils.containsIgnoreCase(membersList.getMembers(), clientFilter.getValue())));
        filterRow.getCell(MembersListColumn).setComponent(clientFilter);

        TextField amountFilter = new TextField();
        amountFilter.setPlaceholder("Filter");
        amountFilter.setClearButtonVisible(true);
        amountFilter.setWidth("100%");
        amountFilter.setValueChangeMode(ValueChangeMode.EAGER);
        amountFilter.addValueChangeListener(event -> gridListDataView.addFilter(client -> StringUtils
                .containsIgnoreCase(Double.toString(client.getAmount()), amountFilter.getValue())));
        filterRow.getCell(amountColumn).setComponent(amountFilter);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.setItems(Arrays.asList("Pending", "Success", "Error"));
        statusFilter.setPlaceholder("Filter");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("100%");
        statusFilter.addValueChangeListener(
                event -> gridListDataView.addFilter(client -> areStatusesEqual(client, statusFilter)));
        filterRow.getCell(statusColumn).setComponent(statusFilter);

        DatePicker dateFilter = new DatePicker();
        dateFilter.setPlaceholder("Filter");
        dateFilter.setClearButtonVisible(true);
        dateFilter.setWidth("100%");
        dateFilter.addValueChangeListener(
                event -> gridListDataView.addFilter(client -> areDatesEqual(client, dateFilter)));
        filterRow.getCell(dateColumn).setComponent(dateFilter);
    }

    private boolean areStatusesEqual(MembersList membersList, ComboBox<String> statusFilter) {
        String statusFilterValue = statusFilter.getValue();
        if (statusFilterValue != null) {
            return StringUtils.equals(membersList.getStatus(), statusFilterValue);
        }
        return true;
    }

    private boolean areDatesEqual(MembersList membersList, DatePicker dateFilter) {
        LocalDate dateFilterValue = dateFilter.getValue();
        if (dateFilterValue != null) {
            LocalDate clientDate = LocalDate.parse(membersList.getDate());
            return dateFilterValue.equals(clientDate);
        }
        return true;
    }

    private List<MembersList> getMembers() {
        return Arrays.asList(
                createClient(4957, "", "Richard Torres", 47427.0,
                        "Active", "2019-05-09"),
                createClient(675, "", "Dakota Hobson", 70503.0,
                        "Active", "2019-05-09"),
                createClient(6816, "", "Michael Roqueta", 58931.0,
                        "Active", "2019-05-07"),
                createClient(5144, "", "Damon Davis ", 25053.0,
                        "Active", "2019-04-25"),
                createClient(9800, "", "", 0.0,
                        "Error", "2019-04-22"),
                createClient(3599, "", "", 0.0,
                        "Error", "2019-04-17"),
                createClient(3989, "", "", 0.0, "Error",
                        "2019-04-17"),
                createClient(1077, "", "", 0.0,
                        "Error", "2019-02-26"),
                createClient(8942, "", "", 0.0,
                        "Error", "2019-02-21"));
    }

    private MembersList createClient(int id, String img, String members, double amount, String status, String date) {
        MembersList c = new MembersList();
        c.setId(id);
        c.setImg(img);
        c.setMembers(members);
        c.setAmount(amount);
        c.setStatus(status);
        c.setDate(date);

        return c;
    }
};
