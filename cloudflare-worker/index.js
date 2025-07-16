/**
 * REAI Email Voucher Reception Worker (JavaScript edition)
 * Handles incoming e-mails, extracts <25 MB attachments,
 * and uploads them to the REAI voucher API.
 */

import PostalMime from 'postal-mime';

const MAX_FILE_SIZE = 25 * 1024 * 1024; // 25 MB

export default {
    /** Cloudflare Email Worker entry-point */
    async email(message, env, ctx) {
        try {
            if (message.rawSize > MAX_FILE_SIZE * 2) {            // reject really large mails early
                console.log(`Email too large: ${message.rawSize} bytes`);
                return;
            }

            // Parse the raw RFC-822 message
            const rawEmail = await new Response(message.raw).arrayBuffer();
            const email = await PostalMime.parse(rawEmail, { attachmentEncoding: 'base64' });

            // Derive company slug from recipient local-part (acme@ea.reai.no → acme)
            const companySlug = (message.to || '').split('@')[0].toLowerCase();
            if (!companySlug) {
                console.error('Could not extract company slug from recipient:', message.to);
                return;
            }

            // Iterate over attachments
            for (const attachment of email.attachments ?? []) {
                const fileData = attachment.content;                // already base64
                if (!fileData) continue;

                // Rough size estimate (Base64 ≈ +33 %)
                const estimatedSize = Math.floor((fileData.length * 3) / 4);
                if (estimatedSize > MAX_FILE_SIZE) {
                    console.log(`Skipping ${attachment.filename}: ${estimatedSize} > 25 MB`);
                    continue;
                }

                await sendToReaiApi(companySlug, {
                    filename: attachment.filename || 'unnamed_file',
                    mimeType: attachment.mimeType || 'application/octet-stream',
                    fileSize: estimatedSize,
                    fileData,
                    senderEmail: message.from
                }, env);
            }
        } catch (err) {
            console.error('Error processing email:', err);
        }
    },

    /** Simple health-check for HTTP requests */
    async fetch(request) {
        if (request.method === 'GET' && new URL(request.url).pathname === '/health') {
            return new Response('OK', { status: 200 });
        }
        return new Response('Email worker active', { status: 200 });
    }
};

/** Upload one document to the REAI backend */
async function sendToReaiApi(companySlug, payload, env) {
    const res = await fetch(`${env.VOUCHER_API_BASE}/documents`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Company-Slug': companySlug,
            'Authorization': `Bearer ${env.API_TOKEN}`
        },
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        throw new Error(`API request failed: ${res.status} ${res.statusText}`);
    }
    console.log(`Uploaded ${payload.filename} for ${companySlug}`);
}
