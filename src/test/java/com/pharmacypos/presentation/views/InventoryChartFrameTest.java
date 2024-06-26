package com.pharmacyPOS.presentation.views;

import static org.junit.jupiter.api.Assertions.*;

import com.pharmacyPOS.data.dao.InventoryDao;
import com.pharmacyPOS.data.database.DatabaseConnection;
import com.pharmacyPOS.presentation.controllers.InventoryController;
import com.pharmacyPOS.service.InventoryService;
import org.jfree.chart.ChartPanel;
import org.junit.jupiter.api.Test;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

class InventoryChartFrameTest {

    @Test
    void createChart_ShouldReturnValidJFreeChart() {
        // Arrange
        DatabaseConnection connection = new DatabaseConnection();
        connection.connect();
        InventoryController inventoryController = new InventoryController(new InventoryService(new InventoryDao(connection)));
        InventoryChartFrame frame = new InventoryChartFrame(inventoryController);

        // Act
        JFreeChart chart = frame.createChart();

        // Assert
        assertNotNull(chart);
        assertEquals("Inventory Report", chart.getTitle().getText());

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        CategoryAxis domainAxis = plot.getDomainAxis();
        assertEquals("Product", domainAxis.getLabel());

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        assertEquals("Quantity", rangeAxis.getLabel());

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        assertNotNull(renderer);
    }

    @Test
    void initUI_ShouldSetContentPanelWithChartPanel() {
        // Arrange
        DatabaseConnection connection = new DatabaseConnection();
        connection.connect();
        InventoryController inventoryController = new InventoryController(new InventoryService(new InventoryDao(connection)));        InventoryChartFrame frame = new InventoryChartFrame(inventoryController);

        // Act
        frame.initUI();

        // Assert
        assertNotNull(frame.getContentPane().getComponent(0));
        assertTrue(frame.getContentPane().getComponent(0) instanceof ChartPanel);
    }

    @Test
    void main_ShouldCreateInventoryChartFrame() {
        // Act
        InventoryChartFrame.main(new String[]{});
    }

}

