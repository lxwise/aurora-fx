package io.data.chain.fx.concurrent.model;

import javafx.concurrent.Service;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * ServiceListCell 的工厂类。
 * 用于 ListView.setCellFactory()。
 */
public class ServiceListCellFactory<T> implements Callback<ListView<Service<T>>, ListCell<Service<T>>> {
    @Override
    public ListCell<Service<T>> call(ListView<Service<T>> listView) {
        return new ServiceListCell<>();
    }
}
