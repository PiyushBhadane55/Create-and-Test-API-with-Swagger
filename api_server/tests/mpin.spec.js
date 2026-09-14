const { test, expect } = require('@playwright/test');

test.describe('MPIN Authentication & Verification API Endpoints', () => {

  const SEED_USER = 'USER1001';
  const SEED_MPIN = '4829';

  test('GET /api/mpin/status/:userId - should return initial MPIN status for seed user', async ({ request }) => {
    const response = await request.get(`/api/mpin/status/${SEED_USER}`);
    expect(response.status()).toBe(200);

    const status = await response.json();
    expect(status.userId).toBe(SEED_USER);
    expect(status.isMpinSet).toBe(true);
    expect(status.isLocked).toBe(false);
    expect(status.failedAttempts).toBe(0);
    expect(status.maxAttempts).toBe(3);
  });

  test('POST /api/mpin/setup - should reject weak MPINs (sequential / repeating)', async ({ request }) => {
    const userId1 = 'WEAK_USER_1_' + Date.now();
    const userId2 = 'WEAK_USER_2_' + Date.now();

    // Test sequential MPIN "1234"
    const seqRes = await request.post('/api/mpin/setup', {
      data: { userId: userId1, mpin: '1234', confirmMpin: '1234' }
    });
    expect(seqRes.status()).toBe(400);
    const seqData = await seqRes.json();
    expect(seqData.success).toBe(false);
    expect(seqData.status).toBe('WEAK_MPIN');

    // Test repeating MPIN "1111"
    const repRes = await request.post('/api/mpin/setup', {
      data: { userId: userId2, mpin: '1111', confirmMpin: '1111' }
    });
    expect(repRes.status()).toBe(400);
    const repData = await repRes.json();
    expect(repData.success).toBe(false);
    expect(repData.status).toBe('WEAK_MPIN');
  });

  test('POST /api/mpin/setup - should successfully set up valid MPIN for new user', async ({ request }) => {
    const newUserId = 'USER_NEW_' + Date.now();
    const validMpin = '8419';

    const response = await request.post('/api/mpin/setup', {
      data: { userId: newUserId, mpin: validMpin, confirmMpin: validMpin }
    });
    expect(response.status()).toBe(201);

    const result = await response.json();
    expect(result.success).toBe(true);
    expect(result.status).toBe('MPIN_SETUP_SUCCESS');
  });

  test('POST /api/mpin/verify - should verify correct MPIN successfully', async ({ request }) => {
    const userId = 'VERIFY_USER_' + Date.now();
    const mpin = '6193';

    await request.post('/api/mpin/setup', {
      data: { userId, mpin, confirmMpin: mpin }
    });

    const response = await request.post('/api/mpin/verify', {
      data: { userId, mpin }
    });
    expect(response.status()).toBe(200);

    const result = await response.json();
    expect(result.success).toBe(true);
    expect(result.status).toBe('MPIN_VERIFIED_SUCCESS');
    expect(result.accountLocked).toBe(false);
  });

  test('POST /api/mpin/verify - should return 401 and decrement remaining attempts on wrong MPIN', async ({ request }) => {
    const userId = 'TEST_ATTEMPTS_USER_' + Date.now();
    const mpin = '9372';

    // 1. Setup user
    await request.post('/api/mpin/setup', {
      data: { userId, mpin, confirmMpin: mpin }
    });

    // 2. Attempt 1 wrong MPIN
    const res1 = await request.post('/api/mpin/verify', {
      data: { userId, mpin: '0001' }
    });
    expect(res1.status()).toBe(401);
    const data1 = await res1.json();
    expect(data1.success).toBe(false);
    expect(data1.remainingAttempts).toBe(2);

    // 3. Attempt 2 wrong MPIN
    const res2 = await request.post('/api/mpin/verify', {
      data: { userId, mpin: '0002' }
    });
    expect(res2.status()).toBe(401);
    const data2 = await res2.json();
    expect(data2.remainingAttempts).toBe(1);
  });

  test('POST /api/mpin/verify - 3 consecutive failures should lock account (HTTP 423 Locked)', async ({ request }) => {
    const userId = 'LOCK_TEST_USER_' + Date.now();
    const mpin = '5924';

    // Setup user
    await request.post('/api/mpin/setup', {
      data: { userId, mpin, confirmMpin: mpin }
    });

    // Fail 3 times
    await request.post('/api/mpin/verify', { data: { userId, mpin: '1112' } });
    await request.post('/api/mpin/verify', { data: { userId, mpin: '1113' } });
    const lockRes = await request.post('/api/mpin/verify', { data: { userId, mpin: '1114' } });

    expect(lockRes.status()).toBe(423); // HTTP 423 Locked
    const lockData = await lockRes.json();
    expect(lockData.success).toBe(false);
    expect(lockData.status).toBe('ACCOUNT_LOCKED');
    expect(lockData.accountLocked).toBe(true);
    expect(lockData.remainingAttempts).toBe(0);

    // Verify subsequent attempt (even with correct MPIN) is blocked due to lock
    const blockedRes = await request.post('/api/mpin/verify', { data: { userId, mpin } });
    expect(blockedRes.status()).toBe(423);
  });

  test('POST /api/mpin/reset - should unlock account and reset MPIN using OTP reset token', async ({ request }) => {
    const userId = 'RESET_USER_' + Date.now();
    const oldMpin = '7412';
    const newMpin = '9531';

    // Setup & Lock account
    await request.post('/api/mpin/setup', { data: { userId, mpin: oldMpin, confirmMpin: oldMpin } });
    await request.post('/api/mpin/verify', { data: { userId, mpin: '0001' } });
    await request.post('/api/mpin/verify', { data: { userId, mpin: '0002' } });
    await request.post('/api/mpin/verify', { data: { userId, mpin: '0003' } });

    // Reset MPIN
    const resetRes = await request.post('/api/mpin/reset', {
      data: { userId, resetToken: '123456', newMpin, confirmNewMpin: newMpin }
    });
    expect(resetRes.status()).toBe(200);
    const resetData = await resetRes.json();
    expect(resetData.success).toBe(true);
    expect(resetData.status).toBe('MPIN_RESET_SUCCESS');

    // Verify authentication succeeds with new MPIN
    const verifyRes = await request.post('/api/mpin/verify', { data: { userId, mpin: newMpin } });
    expect(verifyRes.status()).toBe(200);
  });

  test('POST /api/mpin/change - should change MPIN when old MPIN is valid', async ({ request }) => {
    const userId = 'CHANGE_USER_' + Date.now();
    const oldMpin = '3819';
    const newMpin = '6204';

    await request.post('/api/mpin/setup', { data: { userId, mpin: oldMpin, confirmMpin: oldMpin } });

    const changeRes = await request.post('/api/mpin/change', {
      data: { userId, oldMpin, newMpin, confirmNewMpin: newMpin }
    });
    expect(changeRes.status()).toBe(200);

    const changeData = await changeRes.json();
    expect(changeData.success).toBe(true);

    // Verify old MPIN fails, new MPIN succeeds
    const oldVerify = await request.post('/api/mpin/verify', { data: { userId, mpin: oldMpin } });
    expect(oldVerify.status()).toBe(401);

    const newVerify = await request.post('/api/mpin/verify', { data: { userId, mpin: newMpin } });
    expect(newVerify.status()).toBe(200);
  });
});
