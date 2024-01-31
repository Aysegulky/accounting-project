# Web-Based Accounting System

## Introduction
This project is a web-based accounting system designed as a Software as a Service (SaaS). It offers a comprehensive suite of features tailored for efficient accounting management, including product categorization, invoice generation, stock-based reporting, multi-role user management, and multi-company support.

## Features
- **Product Management:** Create and manage products by category.
- **Invoice Generation:** Create purchase and sales invoices with automatic stock adjustments.
- **Stock-Based Reports:** Generate detailed reports based on current stock levels.
- **Role-Based Access Control:** Assign users with roles such as ADMIN, MANAGER, and EMPLOYEE.
- **Multi-Company Support:** Manage accounting processes for separate companies, each with its own set of data and users.
- **Root Administration:** A ROOT user can create companies and assign ADMIN users.
- **Dashboard Statistics:** View key statistics on the dashboard, including total cost, total sales, and currency information for the current year.
- **Profit & Loss Calculation:** Utilize the FIFO approach for accurate profit and loss calculations.

## Technology Stack
- Java 11
- Spring Boot 2.7.11
- Spring MVC
- Thymeleaf, HTML, CSS for UI
- Spring Data, JPA & Hibernate
- PostgreSQL
- Maven
- JUnit5 and Mockito for testing
- Lombok 
- Spring Cloud Open Feign

## Application Overview

# Login Page
![Login Page](src/main/resources/static/images/app-view/login.png)

# Root User View
<li>
Root users have the capability to establish companies and designate their admin users, granting client companies system access. They have the power to enable or disable companies. When a company is disabled, its users lose access to the system.
</li>

![Root User](src/main/resources/static/images/app-view/root1.png)
![Root User](src/main/resources/static/images/app-view/root2.png)
![Root User](src/main/resources/static/images/app-view/root3.png)

# Client Company Admin View

## User Registration

<li>
The root company can create admins, whereas the admins of client companies have the capability to create users (admin, manager, employee) for their own company.
</li>

![Admin User](src/main/resources/static/images/app-view/user-list.png)


![Admin User](src/main/resources/static/images/app-view/user-create.png)

## Client/Vendor Registration
<li>
Admins can create clients and vendors for their company.
</li>

![Admin User](src/main/resources/static/images/app-view/client-vendor-list.png)

![Admin User](src/main/resources/static/images/app-view/client-vendor-create.png)

## Stock Management
<li>
Admins can create categories and products for their company.
</li>

![Admin User](src/main/resources/static/images/app-view/category-list.png)

![Admin User](src/main/resources/static/images/app-view/product-list.png)

## Invoices
<li>
Admins create invoices for purchases or sales. The app auto-generates numbers and dates for these invoices. Purchase invoices boost stock, while sales invoices reduce it. Stocks update only after a manager approves an invoice. The system blocks sales invoices for products lacking enough stock, preventing sales of unavailable items. Any Invoice can be printed after approval.
</li>

![Admin User](src/main/resources/static/images/app-view/invoice-list.png)

![Admin User](src/main/resources/static/images/app-view/invoice-create.png)

![Admin User](src/main/resources/static/images/app-view/invoice-update.png)

![Admin User](src/main/resources/static/images/app-view/print-invoice.png)

## Reporting
<li>
On the profit and loss page, admins view monthly financial outcomes. The product profit and loss page shows each item's performance. The stock report tracks inventory adjustments. The dashboard displays the company's total costs, sales, and overall financial health.
</li>

![Admin User](src/main/resources/static/images/app-view/monthly-profit-loss.png)

![Admin User](src/main/resources/static/images/app-view/product-specific-profit-loss.png)

![Admin User](src/main/resources/static/images/app-view/stock-report.png)

![Admin User](src/main/resources/static/images/app-view/dashboard.png)

#### Profit/Loss Calculated Based On:

![Admin User](src/main/resources/static/images/app-view/profit-loss-calculation.png)

## Membership Payment

<li>
Admins can manage monthly subscription payments on the membership page, utilizing Stripe for secure transactions.
</li>

![Admin User](src/main/resources/static/images/app-view/payment1.png)

![Admin User](src/main/resources/static/images/app-view/payment2.png)

![Admin User](src/main/resources/static/images/app-view/payment3.png)

![Admin User](src/main/resources/static/images/app-view/payment4.png)