
import React from 'react';
import type { CategoryBreakdownResponse } from '../../api/types';
import './CategoryChart.css';

interface CategoryChartProps {
  data: CategoryBreakdownResponse[];
}

export function CategoryChart({ data }: CategoryChartProps) {
  const maxAmount = Math.max(...data.map((d) => d.totalAmount), 1);

  return (
    <div className="category-chart">
      {data.length === 0 ? (
        <p className="no-data">No spending data available</p>
      ) : (
        data.map((category) => (
          <div key={category.categoryId} className="category-bar-item">
            <div className="category-info">
              <span className="category-icon">{category.icon}</span>
              <span className="category-name">{category.categoryName}</span>
            </div>
            <div className="bar-container">
              <div
                className="bar-fill"
                style={{
                  width: `${(category.totalAmount / maxAmount) * 100}%`,
                  backgroundColor: category.color,
                }}
              />
            </div>
            <div className="category-stats">
              <span className="amount">${category.totalAmount.toFixed(2)}</span>
              <span className="percentage">{category.percentage.toFixed(1)}%</span>
            </div>
          </div>
        ))
      )}
    </div>
  );
}
