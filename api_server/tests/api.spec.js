const { test, expect } = require('@playwright/test');

test.describe('Customer REST API Endpoints', () => {

  test('GET /api/customers - should return list of initial seed customers', async ({ request }) => {
    const response = await request.get('/api/customers');
    expect(response.status()).toBe(200);

    const customers = await response.json();
    expect(Array.isArray(customers)).toBe(true);
    expect(customers.length).toBeGreaterThanOrEqual(1);
    expect(customers[0]).toHaveProperty('id');
    expect(customers[0]).toHaveProperty('name');
    expect(customers[0]).toHaveProperty('email');
  });

  test('GET /api/customers/1 - should return Customer #1 details', async ({ request }) => {
    const response = await request.get('/api/customers/1');
    expect(response.status()).toBe(200);

    const customer = await response.json();
    expect(customer.id).toBe(1);
    expect(customer.name).toBeTruthy();
    expect(customer.email).toBeTruthy();
  });

  test('POST /api/customers - should create a new customer record', async ({ request }) => {
    const newCustomer = {
      name: 'Playwright Test User',
      email: 'playwright.user@example.com',
      phone: '+1-555-7777',
    };

    const response = await request.post('/api/customers', {
      data: newCustomer,
    });
    expect(response.status()).toBe(201);

    const created = await response.json();
    expect(created.id).toBeTruthy();
    expect(created.name).toBe(newCustomer.name);
    expect(created.email).toBe(newCustomer.email);
    expect(created.phone).toBe(newCustomer.phone);
  });

  test('PUT /api/customers/1 - should update existing customer details', async ({ request }) => {
    const updatedCustomer = {
      name: 'Alice Updated via Playwright',
      email: 'alice.playwright@example.com',
      phone: '+1-555-8888',
    };

    const response = await request.put('/api/customers/1', {
      data: updatedCustomer,
    });
    expect(response.status()).toBe(200);

    const updated = await response.json();
    expect(updated.id).toBe(1);
    expect(updated.name).toBe(updatedCustomer.name);
    expect(updated.email).toBe(updatedCustomer.email);
  });

  test('DELETE /api/customers/:id - should delete customer record by ID', async ({ request }) => {
    // 1. Create a temporary customer to delete
    const tempCustomer = {
      name: 'User To Delete',
      email: 'delete.me@example.com',
      phone: '+1-555-0000',
    };
    const createRes = await request.post('/api/customers', { data: tempCustomer });
    expect(createRes.status()).toBe(201);
    const created = await createRes.json();
    const targetId = created.id;

    // 2. Delete the created customer
    const deleteResponse = await request.delete(`/api/customers/${targetId}`);
    expect(deleteResponse.status()).toBe(204);

    // 3. Verify 404 on subsequent GET
    const getResponse = await request.get(`/api/customers/${targetId}`);
    expect(getResponse.status()).toBe(404);
  });
});
