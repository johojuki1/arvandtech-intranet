package com.arvandtech.domain.controllers;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;

/**
 * The Navigator module remembers the location and pages the user is using for
 * all the tabs and variants of pages of each tab.
 *
 * @author Jonathan
 */
@Named("navigator")
@SessionScoped
public class Navigator implements Serializable {

    private final String SETTINGSLINK = "/settings/";
    private final String ADDSTOCKLINK = "/addStock/";
    private int mainPageIndex;
    private int settingsIndex;
    private int accountIndex;
    private int addStockIndex;

    /*
    Storage of current location of main tab system. (The one that is permanately visible of left of all pages.
     */
    public void mainTabChange(TabChangeEvent event) {
        TabView tabView = (TabView) event.getComponent();
        mainPageIndex = tabView.getChildren().indexOf(event.getTab());
    }

    /*
    Storage of current location of main tab system. (The one that is permanately visible of left of all pages.
     */
    public void settingsTabChange(TabChangeEvent event) {
        TabView tabView = (TabView) event.getComponent();
        settingsIndex = tabView.getChildren().indexOf(event.getTab());
    }

    /*
    Returns user's current location within the account management system. 
     */
    public String getAccountLink() {
        switch (accountIndex) {
            case 0:
                return SETTINGSLINK + "accountManagement/accountManagement.xhtml";
            case 1:
                return SETTINGSLINK + "accountManagement/createAccount.xhtml";
            case 2:
                return SETTINGSLINK + "accountManagement/editAccount.xhtml";
            case 3:
                return SETTINGSLINK + "accountManagement/changePassword.xhtml";
            default:
                accountIndex = 0;
                return SETTINGSLINK + "accountManagement/accountManagement.xhtml";
        }
    }

    /*
    Returns user's current location within the add stock pages. 
     */
    public String getAddStockLink() {
        switch (addStockIndex) {
            case 0:
                return ADDSTOCKLINK + "pageElements/typeSelect.xhtml";
            default:
                addStockIndex = 0;
                return ADDSTOCKLINK + "pageElements/typeSelect.xhtml";
        }
    }

    //GETTERS
    public int getMainPageIndex() {
        return mainPageIndex;
    }

    public int getSettingsIndex() {
        return settingsIndex;
    }

    //SETTERS
    public void setMainPageIndex(int mainPageIndex) {
        this.mainPageIndex = mainPageIndex;
    }

    public void setSettingsIndex(int settingsIndex) {
        this.settingsIndex = settingsIndex;
    }
    public void setAccountIndex(int accountIndex) {
        this.accountIndex = accountIndex;
    }

    public void setAddStockIndex(int addStockIndex) {
        this.addStockIndex = addStockIndex;
    }
}
