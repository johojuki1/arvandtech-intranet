/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arvandtech.controllers;

import com.arvandtech.domain.entities.Attribute;
import com.arvandtech.domain.entities.ItemType;
import com.arvandtech.domain.facades.ItemTypeFacade;
import com.arvandtech.domain.entities.SelectableBox;
import com.arvandtech.domain.entities.ItemAttribute;
import com.arvandtech.domain.entities.SecondaryAttribute;
import com.arvandtech.domain.entities.Tracked;
import com.arvandtech.domain.entities.TrackedItem;
import com.arvandtech.domain.facades.SecondaryAttributeFacade;
import com.arvandtech.domain.facades.SelectableBoxFacade;
import com.arvandtech.utilities.ScanBarcodeTable;
import com.arvandtech.utilities.Settings;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

/**
 * Controller used to manage all functions used by the add stock function.
 *
 * @author User
 */
@Named("addStock")
@SessionScoped
public class AddStockController implements Serializable {

    @EJB
    private ItemTypeFacade itemFacade;

    @EJB
    private SelectableBoxFacade selectFacade;

    @EJB
    private SecondaryAttributeFacade secondaryFacade;

    private ItemType item;
    private ArrayList<ItemAttribute> attributes;
    private ArrayList<SelectableBox> selections;
    private ArrayList<SecondaryAttribute> secondaries;
    private Tracked selectedAttributes;
    private String barcode;
    private String status;
    private String itemCondition;
    private String orderNo;
    private String description;
    private ArrayList<ScanBarcodeTable> tableItems;
    private ScanBarcodeTable selectedTableItem;
    private boolean tableStateAdd;

    public List<ItemType> findAllItems() {
        return itemFacade.findAll();
    }

    public boolean checkAttributeExist() {
        attributes = new ArrayList<>();
        selections = new ArrayList<>();
        secondaries = new ArrayList<>();
        try {
            for (int i = 0; i < new Settings().getMAX_ATTRIBUTES(); i++) {
                try {
                    item.getAttribute().get(i);
                    attributes.add(new ItemAttribute());
                    selections.add(new SelectableBox());
                    secondaries.add(new SecondaryAttribute());
                } catch (Exception e) {
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void resetAll() {
        status = new String();
        itemCondition = new String();
        selectedAttributes = new Tracked();
        secondaries = new ArrayList<>();
        selections = new ArrayList<>();
        attributes = new ArrayList<>();
        orderNo = new String();
    }

    public String findTitle(int i) {
        return item.getAttribute().get(i - 1).getAttributeName();
    }

    public String findSecondaryTitle(int i) {
        return selections.get(i - 1).getSecondaryName();
    }

    public void clearItem() {
        item = new ItemType();
    }

    public boolean attributeExists(int i) {
        try {
            return i <= item.getAttribute().size();
        } catch (Exception e) {
            return false;
        }
    }

    /*
    Function is pointless and only used to trigger onchange.
    onchange needs to be triggered for the 'findSecondaryLink' function to recieve its correct id. Otherwise the id returns as 0 all of the time.
    I tried to use ajax to trigger the ID, but the attribute 'select' on the html does not persist until 'findSecondaryLink' triggers.
    Many solutions were trialed including having a saparate listener as an ajax command and having the 'onchange' command do soemthing.
    None worked due to load orders of HTML or 'select' attribute no longer existing.
    This non-sensical solution is the only one tested that works short of making a different attribute for every 'SelectableBox' object. 
     */
    public void listener() {
    }

    public void updateSecondaryAttribute(int i, int id) {
        if (id != 0) {
            SecondaryAttribute tmpsa = secondaryFacade.find(id);
            secondaries.set(i - 1, tmpsa);
        }
    }

    public String findLink(int i) {
        if (attributeExists(i)) {
            Attribute tmpAtt = item.getAttribute().get(i - 1);
            if (tmpAtt.isSelectable()) {
                return "layouts/selectable.xhtml";
            } else {
                return "layouts/entry.xhtml";
            }
        }
        return "";
    }

    public String findSecondaryLink(int i, int id) {
        try {
            if (id == -1) {
                selections.set(i - 1, new SelectableBox());
            } else if (id != 0) {
                SelectableBox tmpsb = selectFacade.find(id);
                selections.set(i - 1, tmpsb);
                ItemAttribute tmpAtt = new ItemAttribute();
                tmpAtt.setAttributeName(item.getAttribute().get(i - 1).getAttributeName());
                tmpAtt.setAttributeValue(tmpsb.getName());
                attributes.set(i - 1, tmpAtt);
            }
            if (selections.get(i - 1).getName() != null && selections.get(i - 1).isSecondary()) {
                return "secondarySelection.xhtml";
            }
        } catch (Exception e) {
        }
        return "";
    }

    public int moveToScan() {
        selectedAttributes = new Tracked();
        boolean errorChecker = true;
        if (!checkEmpty(itemCondition, "Item condition cannot be empty", "submitButton")) {
            errorChecker = false;
        }
        if (!checkEmpty(status, "Item status cannot be empty", "submitButton")) {
            errorChecker = false;
        }
        if (!checkEmpty(orderNo, "Item order number cannot be empty", "submitButton")) {
            errorChecker = false;
        }
        if (!errorChecker) {
            return 1;
        }
        selectedAttributes.setItemCondition(itemCondition);
        selectedAttributes.setStatus(status);
        selectedAttributes.setItemTypeName(item.getTypeName());
        List<TrackedItem> tmpAttributeList = new ArrayList<>();
        for (int i = 0; i < item.getAttribute().size(); i++) {
            TrackedItem tmpAttribute = new TrackedItem();
            tmpAttribute.setAttribute(attributes.get(i));
            tmpAttribute.setItemOrder(item.getAttribute().get(i).getAttributeOrder());
            if (selections.get(i).isSecondary()) {
                tmpAttribute.getAttribute().setSecondaryName(selections.get(i).getSecondaryName());
                tmpAttribute.getAttribute().setSecondaryValue(secondaries.get(i).getSecondaryAttributeName());
            }
            tmpAttributeList.add(tmpAttribute);
            if (!checkEmpty(tmpAttribute.getAttribute().getAttributeValue(), "All attribute fields must be filled in and cannot be empty.", "submitButton")) {
                return 1;
            }
        }
        selectedAttributes.setAttributes(tmpAttributeList);
        tableItems = new ArrayList();
        initiateAdd();
        return 2;
    }

    public boolean checkEmpty(String string, String fieldEmptyError, String errorBoxName) {
        if (string != null) {
            if (!(string.isEmpty() || string.equals("-1"))) {
                return true;
            }
        }
        FacesContext.getCurrentInstance().addMessage(errorBoxName, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error:", fieldEmptyError));
        return false;
    }

    public void addManualInputs(ActionEvent event) {
        for (int i = 0; i < new Settings().getMAX_ATTRIBUTES(); i++) {
            String tmpManual = (String) event.getComponent().getAttributes().get("man" + (i + 1));
            if (tmpManual != null) {
                if (!tmpManual.equals("")) {
                    attributes.get(i).setAttributeName(item.getAttribute().get(i).getAttributeName());
                    attributes.get(i).setAttributeValue(tmpManual);
                }
            }
        }
    }

    public List<SelectableBox> findSelectables(int i) {
        Attribute tmpAtt = item.getAttribute().get(i - 1);
        tmpAtt.getSelectableBox().sort(Comparator.comparing(SelectableBox::getSelectableOrder));
        return tmpAtt.getSelectableBox();
    }

    public List<SecondaryAttribute> findSecondaries(int i) {
        try {
            selections.get(i - 1).getSecondaryAttribute().sort(Comparator.comparing(SecondaryAttribute::getSecondaryOrder));
            return selections.get(i - 1).getSecondaryAttribute();
        } catch (Exception e) {
        }
        return null;
    }

    public List<String> fetchStatusDropdown() {
        return new Settings().getStatus();
    }

    public List<String> fetchConditionDropdown() {
        return new Settings().getCondition();
    }

    public String fetchAttributeValue(int number, boolean isPrimary, boolean isTitle) {
        try {
            if (isPrimary) {
                if (isTitle) {
                    return (selectedAttributes.getAttributes().get(number - 1).getAttribute().getAttributeName());
                } else {
                    return (selectedAttributes.getAttributes().get(number - 1).getAttribute().getAttributeValue());
                }
            } else {
                if (isTitle) {
                    return (selectedAttributes.getAttributes().get(number - 1).getAttribute().getSecondaryName());
                } else {
                    if (selectedAttributes.getAttributes().get(number - 1).getAttribute().getSecondaryValue() != null) {
                        return (selectedAttributes.getAttributes().get(number - 1).getAttribute().getSecondaryValue());
                    } else {
                        if (selectedAttributes.getAttributes().get(number - 1).getAttribute().getSecondaryName() != null) {
                            return ("Not Selected");
                        } else {
                            return "";
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "";
        }
    }

    public String checkSecondaryExists(int number) {
        try {
            if (selectedAttributes.getAttributes().get(number - 1).getAttribute().getSecondaryName() != null) {
                return "true";
            }
        } catch (Exception e) {

        }
        return "false";
    }

    public void addBarcodeItem() {
        if (tableStateAdd) {
            ScanBarcodeTable tmpTableItem = new ScanBarcodeTable();
            tmpTableItem.setBarcode(barcode);
            tmpTableItem.setCondition(itemCondition);
            tmpTableItem.setStatus(status);
            tmpTableItem.setDescription(description);
            if (tmpTableItem.getDescription().isEmpty()) {
                tmpTableItem.setDescriptionExists("No");
            } else {
                tmpTableItem.setDescriptionExists("Yes");
            }
            if (tableItems.isEmpty()) {
                tmpTableItem.setId(1);
            } else {
                tmpTableItem.setId(tableItems.get(0).getId() + 1);
            }
            tableItems.add(tmpTableItem);
            initiateAdd();
        }
    }

    public void editBarcodeItem() {
        if (!tableStateAdd) {
            int index = tableItems.indexOf(selectedTableItem);
            selectedTableItem.setBarcode(barcode);
            selectedTableItem.setCondition(itemCondition);
            selectedTableItem.setStatus(status);
            selectedTableItem.setDescription(description);
            if (selectedTableItem.getDescription().isEmpty()) {
                selectedTableItem.setDescriptionExists("No");
            } else {
                selectedTableItem.setDescriptionExists("Yes");
            }
            tableItems.set(index, selectedTableItem);
            initiateAdd();
        }
    }

    public void deleteBarcodeItem() {
        tableItems.remove(selectedTableItem);
        initiateAdd();
    }

    public void initiateEdit() {
        try {
            tableStateAdd = false;
            barcode = selectedTableItem.getBarcode();
            status = selectedTableItem.getStatus();
            itemCondition = selectedTableItem.getCondition();
            description = selectedTableItem.getDescription();
        } catch (Exception e) {

        }
    }

    public void initiateAdd() {
        barcode = "";
        itemCondition = selectedAttributes.getItemCondition();
        status = selectedAttributes.getStatus();
        description = "";
        selectedTableItem = new ScanBarcodeTable();
        tableStateAdd = true;
    }
//GETTERS

    public ItemType getItem() {
        return item;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public ArrayList<ScanBarcodeTable> getTableItems() {
        return tableItems;
    }

    public ScanBarcodeTable getSelectedTableItem() {
        return selectedTableItem;
    }

    public boolean isTableStateAdd() {
        return tableStateAdd;
    }

    //SETTERS
    public void setItem(ItemType item) {
        item.getAttribute().sort(Comparator.comparing(Attribute::getAttributeOrder));
        this.item = item;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public void setTableItems(ArrayList<ScanBarcodeTable> tableItems) {
        this.tableItems = tableItems;
    }

    public void setSelectedTableItem(ScanBarcodeTable selectedTableItem) {
        this.selectedTableItem = selectedTableItem;
    }

    public void setTableStateAdd(boolean tableStateAdd) {
        this.tableStateAdd = tableStateAdd;
    }
}
