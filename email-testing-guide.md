# Email Voucher Testing Guide - Step by Step

## Prerequisites Check
- [ ] REAI app can run locally
- [ ] Cloudflare account with Workers enabled
- [ ] Ngrok installed (`brew install ngrok` or download from ngrok.com)
- [ ] Domain `ea.reai.no` added to Cloudflare

## Step 3: Start Ngrok Tunnel
```bash
# In a new terminal
ngrok http 8080
```
📝 Copy the HTTPS URL shown (like `https://abc123-unique.ngrok-free.app`)

## Step 4: Configure Cloudflare Email Routing (One-time)

### 4.1 Add MX Records
In Cloudflare Dashboard → DNS:
```
Type: MX, Name: ea, Content: route1.mx.cloudflare.net, Priority: 1
Type: MX, Name: ea, Content: route2.mx.cloudflare.net, Priority: 5
Type: MX, Name: ea, Content: route3.mx.cloudflare.net, Priority: 10
```

### 4.2 Enable Email Routing
1. Go to Email → Email Routing
2. Add destination addresses
3. Create routing rule:
   - Match: `*@ea.reai.no`
   - Action: Send to Worker
   - Worker: `reai-email-worker-dev`

## Step 5: Update and Deploy Worker
```bash
cd /home/darkhorse/Documents/reai/cloudflare-worker

# Set the API URL to your ngrok tunnel
npx wrangler secret put VOUCHER_API_BASE --env development
# When prompted, enter: https://YOUR-NGROK-URL.ngrok-free.app/api/voucher-reception

# Deploy the worker
npx wrangler deploy --env development
```
✅ Verify: You should see "Deployed reai-email-worker-dev"

## Step 6: Monitor All Components
Open 4 terminal windows:

### Terminal 1 - REAI App
```bash
# Already running from Step 1
# Watch for incoming POST requests
```

### Terminal 2 - Ngrok
```bash
# Already running from Step 3
# Or check web interface at http://localhost:4040
```

### Terminal 3 - Worker Logs
```bash
npx wrangler tail --env development
```

### Terminal 4 - Database Monitor
```bash
watch -n 2 'psql -U postgres -d reai -c "SELECT id, filename, sender_email, received_at FROM voucher_documents ORDER BY id DESC LIMIT 5;"'
```

## Step 7: Send Test Email

### Option A: Real Email Test
Send an email with attachment to: `test-company@ea.reai.no`

### Option B: Simulate with Script
```bash
# Create a test file
echo "Test invoice content" > test-invoice.txt

# Convert to base64
BASE64_DATA=$(base64 < test-invoice.txt)

# Send to worker-simulated endpoint (if you add one)
# Or use the direct API test below
```

### Option C: Direct Local API Test (Skip Worker)
```bash
curl -X POST http://localhost:8080/api/voucher-reception/documents \
  -H "Content-Type: application/json" \
  -H "X-Company-Slug: test-company" \
  -d '{
    "filename": "test-invoice.pdf",
    "mimeType": "application/pdf",
    "fileSize": 1024,
    "fileData": "VGVzdCBpbnZvaWNlIGNvbnRlbnQ=",
    "senderEmail": "supplier@example.com"
  }'
```

## Step 8: Verify Success

### Check Worker Logs (Terminal 3)
Should see:
```
Processing 1 attachments for company: test-company
Successfully uploaded document test-invoice.pdf
```

### Check Ngrok (Terminal 2 or http://localhost:4040)
Should see:
```
POST /api/voucher-reception/documents 200 OK
```

### Check Database (Terminal 4)
Should see new row with your document

### Check REAI Logs (Terminal 1)
Should see the incoming request and processing

## Troubleshooting

### Email Not Received
1. Check MX records are propagated: `dig MX ea.reai.no`
2. Verify Email Routing is enabled in Cloudflare
3. Check worker is deployed: `npx wrangler deployments list`

### Worker Errors
1. Check logs: `npx wrangler tail --env development`
2. Verify environment variable: `npx wrangler secret list --env development`
3. Test worker health: `curl https://reai-email-worker-dev.YOUR-SUBDOMAIN.workers.dev/health`

### Connection Refused
1. Ensure REAI is running on port 8080
2. Check ngrok is still running and URL hasn't changed
3. Verify Spring Security permits `/api/voucher-reception/**`

### Database Not Updating
1. Check company exists with matching slug
2. Verify tenant context is set correctly
3. Check for SQL errors in REAI logs

## Cleanup After Testing
```bash
# Stop ngrok (Ctrl+C in Terminal 2)
# Stop REAI app (Ctrl+C in Terminal 1)
# Optionally, reset worker to production URL:
npx wrangler secret put VOUCHER_API_BASE --env production
# Enter: https://app.reai.no/api/voucher-reception
```

## Success Checklist
- [ ] Email sent to test-company@ea.reai.no
- [ ] Worker logs show processing
- [ ] Ngrok shows incoming request
- [ ] REAI logs show API hit
- [ ] Database has new voucher_document row
- [ ] File content is stored correctly