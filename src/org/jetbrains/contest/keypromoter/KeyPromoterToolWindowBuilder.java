package org.jetbrains.contest.keypromoter;

import com.intellij.openapi.components.ServiceManager;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.*;

/**
 * Created by athiele on 10.01.2015.
 */
public class KeyPromoterToolWindowBuilder {

    private KeyPromoterPersistentStats statsService = ServiceManager.getService(KeyPromoterPersistentStats
            .class);

    private JButton refreshButton;
    private JPanel panel;
    private JList list1;
    private JButton resetStatisticsButton;
    private String[] topTen = new String[10];

    public KeyPromoterToolWindowBuilder() {
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTopTen();
            }
        });
        resetStatisticsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetStats();
            }
        });
    }

    public JPanel createToolWindowPanel() {
        updateTopTen();
        return panel;
    }

    private void resetStats() {
        Map<String, Integer> stats = statsService.getStats();
        stats.clear();
        statsService.setStats(stats);
        updateTopTen();
    }


    private void updateTopTen() {
        Map<String, Integer> stats = statsService.getStats();

        if (!stats.isEmpty()) {
            List<Map.Entry<String, Integer>> sortedEntries = sortByValues(stats);

            int i = 0;
            for (Map.Entry entry : sortedEntries) {
                topTen[i] = "[" + (i + 1) + "] " + entry.getValue() + " times: " + entry.getKey();
                i++;
                if (i == 10) break;
            }
        } else {
            topTen = new String[10];
        }
        list1.setListData(topTen);
    }

    // Sort function
    static <K, V extends Comparable<? super V>>
    List<Map.Entry<K, V>> sortByValues(Map<K, V> map) {

        List<Map.Entry<K, V>> sortedEntries = new ArrayList<Map.Entry<K, V>>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );

        return sortedEntries;
    }

}
