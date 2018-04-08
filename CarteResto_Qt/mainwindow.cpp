#include "mainwindow.h"
#include "ui_mainwindow.h"
#include "modifyorder.h"
#include "commandes.h"
#include "commande.h"
#include "produits.h"
#include "produit.h"
#include "menu.h"

#include<QPixmap>
#include<QPalette>
#include<QGraphicsOpacityEffect>
#include<QSizePolicy>
#include<QListWidgetItem>
#include<QVector>
#include<QString>
#include<iostream>

// https://youtu.be/wUH_gu2HdQE --> create modal windows
// http://www.qtcentre.org/threads/49475-QLabel-text-in-multiple-colors

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    cmds = new Commandes();
    cmds->retrieveCommandes();

    produits = new Produits();
    produits->retrieveProducts();

    ui->setupUi(this);
    setMenuBar();
    ui->mainToolBar->setVisible(false);
    ui->menuBar->setVisible(false);
    ui->stackedWidget->setCurrentIndex(0);

    x = 100;
    y = 100;
    w = 1400;
    h = 800;

    ui_setup();

    //QObject::connect(ui->tablesListWidget, SIGNAL(itemClicked(QListWidgetItem*)),this, SLOT(onListMailItemClicked(QListWidgetItem*)));
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::ui_setup()
{
    // setting a responsive background image
    this->setStyleSheet(
                "#centralWidget { " /* if we don't do that the following properties will be
                                     * inherited by children causing "Qimage: out of memory, returning null image3
                                     */
                " border-image: url(:images/background.png) 0 0 0 0 stretch stretch;"
                "}");

    ui->widgetLogo->setStyleSheet(
                "#widgetLogo { "
                " border-image: url(:images/logo.png) 0 0 0 0 stretch stretch;"
                "}");

    ui->pwLineEdit->setEchoMode(QLineEdit::Password);

    this->setGeometry(x,y,w,h);
}

QVector<QWidget *> MainWindow::getSectionAndChidlren(int section)
{
    QVector<QWidget *> qv;

    switch(section)
    {
        case 1: qv.append(ui->buttonApero);
                qv.append(ui->horizontalWidgetLS1);
                qv.append(ui->listWidgetApero);
                break;
        case 2: qv.append(ui->buttonEntree);
                qv.append(ui->horizontalWidgetLS2);
                qv.append(ui->listWidgetEntrees);
                break;
        case 3: qv.append(ui->buttonPlat);
                qv.append(ui->horizontalWidgetLS3);
                qv.append(ui->listWidgetPlats);
                break;
        case 4: qv.append(ui->buttonDessert);
                qv.append(ui->horizontalWidgetLS4);
                qv.append(ui->listWidgetDesserts);
                break;
        case 5: qv.append(ui->buttonVin);
                qv.append(ui->horizontalWidgetLS5);
                qv.append(ui->listWidgetVins);
                break;
        default: break;
    }

    return qv;
}

void MainWindow::setActiveSection(int section)
{
    QVector<QWidget *> qv = getSectionAndChidlren(section);

    QGraphicsOpacityEffect *_effect = new QGraphicsOpacityEffect();
    _effect->setOpacity(1.0);
    qv.at(2)->setAttribute(Qt::WA_TransparentForMouseEvents , false);
    qv.at(2)->setStyleSheet("border: 1px solid black");
    qv.at(2)->setGraphicsEffect(_effect);

    qv.at(1)->setVisible(true);
}

void MainWindow::disableSection(int section)
{
    QVector<QWidget *> qv = getSectionAndChidlren(section);

    QGraphicsOpacityEffect *_effect = new QGraphicsOpacityEffect();
    _effect->setOpacity(0.4);
    qv.at(2)->setAttribute(Qt::WA_TransparentForMouseEvents , true);
    qv.at(2)->setStyleSheet("border: 1px solid black");
    qv.at(2)->setGraphicsEffect(_effect);

    QSizePolicy sp_retain = qv.at(1)->sizePolicy();
    sp_retain.setRetainSizeWhenHidden(true);
    qv.at(1)->setSizePolicy(sp_retain);
    qv.at(1)->setVisible(false);
}


void MainWindow::on_connectionButton_clicked()
{
    ui->stackedWidget->setCurrentIndex(1);
    setActiveSection(1);
    disableSection(2);
    disableSection(3);
    disableSection(4);
    disableSection(5);

    ui->mainToolBar->setVisible(true);
    ui->menuBar->setVisible(true);

    // background color for the widget that contains all the informations for a table
    QPalette pal = palette();
    pal.setColor(QPalette::Background, Qt::white);
    ui->verticalWidgetTable->setAutoFillBackground(true);
    ui->verticalWidgetTable->setPalette(pal);

    fillTableListWidget();
}

void MainWindow::on_buttonApero_clicked()
{
    setActiveSection(1);
    disableSection(2);
    disableSection(3);
    disableSection(4);
    disableSection(5);
}

void MainWindow::on_buttonEntree_clicked()
{
    disableSection(1);
    setActiveSection(2);
    disableSection(3);
    disableSection(4);
    disableSection(5);
}

void MainWindow::on_buttonPlat_clicked()
{
    disableSection(1);
    disableSection(2);
    setActiveSection(3);
    disableSection(4);
    disableSection(5);
}

void MainWindow::on_buttonDessert_clicked()
{
    disableSection(1);
    disableSection(2);
    disableSection(3);
    setActiveSection(4);
    disableSection(5);
}

void MainWindow::on_buttonVin_clicked()
{
    disableSection(1);
    disableSection(2);
    disableSection(3);
    disableSection(4);
    setActiveSection(5);
}

void MainWindow::on_buttonModifier_clicked()
{
    ModifyOrder mo;
    mo.setModal(true);
    mo.exec();
}

void MainWindow::setMenuBar()
{
    QMenuBar * menuBar = this->menuBar();

    QMenu * optionMenu = menuBar->addMenu( tr("&Options") );

    /* Options */

    QAction * logOutAction = new QAction( QIcon(":images/quit.png"), tr("&Log out"), this);
    QAction * ordersAction = new QAction( QIcon(":images/commandes.png"), tr("&Commandes"), this);
    QAction * modifyCard = new QAction( QIcon(":images/edit.png"), tr("&Modifier la carte"), this);

    logOutAction->setShortcut(tr("Ctrl+L"));
    logOutAction->setToolTip(tr("Log out"));
    logOutAction->setStatusTip(tr("Log out"));

    QObject::connect(logOutAction,SIGNAL(triggered()),this,SLOT(logOut()));

    ordersAction->setShortcut(tr("Ctrl+O"));
    ordersAction->setToolTip(tr("Commandes"));
    ordersAction->setStatusTip(tr("Commandes"));

    QObject::connect(ordersAction,SIGNAL(triggered()),this,SLOT(displayOrdersWidget()));

    modifyCard->setShortcut(tr("Ctrl+M"));
    modifyCard->setToolTip(tr("Modifier la carte"));
    modifyCard->setStatusTip(tr("Modifier la carte"));

    QObject::connect(modifyCard,SIGNAL(triggered()),this,SLOT(displayModifyCardWidget()));

    optionMenu->addAction(logOutAction);
    optionMenu->addAction(ordersAction);
    optionMenu->addAction(modifyCard);

    ui->mainToolBar->addAction(logOutAction);
    ui->mainToolBar->addAction(ordersAction);
    ui->mainToolBar->addAction(modifyCard);
}

void MainWindow::logOut()
{
    ui->mainToolBar->setVisible(false);
    ui->menuBar->setVisible(false);
    ui->stackedWidget->setCurrentIndex(0);
}

void MainWindow::displayOrdersWidget()
{
    if( !ui->pageCommandes->isVisible() )
        ui->stackedWidget->setCurrentIndex(1);
}

void MainWindow::displayModifyCardWidget()
{
    if( !ui->pageAjouter->isVisible() )
        ui->stackedWidget->setCurrentIndex(2);
}

void MainWindow::fillTableListWidget()
{
    int nbCommandes = cmds->getNbCommandes();
    for( int i = 0 ; i < nbCommandes ; ++i )
    {
        QString s1 = QString::fromStdString("Table ");
        QString s2 = QString::number( cmds->getTable(i) );
        tablesItems.push_back( new QListWidgetItem( s1 + s2 , ui->tablesListWidget) );
    }
}

void MainWindow::fillAEPDVListsWidgets( int tableNb )
{
    ui->listWidgetApero->clear();
    ui->listWidgetEntrees->clear();
    ui->listWidgetPlats->clear();
    ui->listWidgetDesserts->clear();
    ui->listWidgetVins->clear();

    int nbCommandes = cmds->getNbCommandes();
    for( int i = 0 ; i < nbCommandes ; ++i )
    {
        Commande currentCmd = cmds->getCommande( i );

        if( currentCmd.getTable() != tableNb ) continue;

        int nbProduitsCmd = currentCmd.getNbProduits();

        for( int j = 0 ; j < nbProduitsCmd ; ++j )
        {
            double currentPId =  currentCmd.getProduitId( j );

            Produit currentP = findProduit( currentPId );

            QString type = currentP.getType();

            if( type == "apero" )
            {

                new QListWidgetItem( currentP.getName() , ui->listWidgetApero);
            }
            else if( type == "entree" )
            {
                new QListWidgetItem( currentP.getName() , ui->listWidgetEntrees);
            }
            else if( type == "plat" )
            {
                new QListWidgetItem( currentP.getName() , ui->listWidgetPlats);
            }
            else if( type == "dessert" )
            {
                new QListWidgetItem( currentP.getName() , ui->listWidgetDesserts);
            }
            else if( type == "vin" )
            {
                new QListWidgetItem( currentP.getName() , ui->listWidgetVins);
            }
            else
            {
                std::cout << "erreur: type is \"" << type.toStdString() << "\"" << std::endl;
            }
        }

        int nbMenusCmd = currentCmd.getNbMenus();

        for( int i = 0 ; i < nbMenusCmd ; ++i)
        {
            Menu m = currentCmd.getMenu( i );

            int nbProduitsMenu = m.getNbProduits();

            for( int j = 0 ; j < nbProduitsMenu ; ++j )
            {
                double currentPId =  m.getIdProduits( j );

                Produit currentP = findProduit( currentPId );

                QString type = currentP.getType();

                if( type == "apero" )
                {
                    new QListWidgetItem( currentP.getName() , ui->listWidgetApero);
                }
                else if( type == "entree" )
                {
                    new QListWidgetItem( currentP.getName() , ui->listWidgetEntrees);
                }
                else if( type == "plat" )
                {
                    new QListWidgetItem( currentP.getName() , ui->listWidgetPlats);
                }
                else if( type == "dessert" )
                {
                    new QListWidgetItem( currentP.getName() , ui->listWidgetDesserts);
                }
                else if( type == "vin" )
                {
                    new QListWidgetItem( currentP.getName() , ui->listWidgetVins);
                }
                else
                {
                    std::cout << "erreur: type is \"" << type.toStdString() << "\"" << std::endl;
                }
            }
        }
    }
}

Produit MainWindow::findProduit( double idProduit )
{
    return produits->findProduit( idProduit );
}

void MainWindow::on_tablesListWidget_itemClicked( QListWidgetItem * item )
{
    fillAEPDVListsWidgets( getTableNumberFrom( item->text() ) );
}

int MainWindow::getTableNumberFrom( QString s )
{
    QStringList qsl = s.split(" ");
    qsl.removeAt(0);
    return qsl.join("").toInt();
}


