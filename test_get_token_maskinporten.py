#!/usr/bin/env python3
"""
Maskinporten Token Client

This script reads a JWK private key from maskinporten_private_key.json
and requests an access token from Maskinporten test environment.
"""

import base64
import json
import time
import uuid

import jwt  # This should be PyJWT
import requests
from cryptography.hazmat.primitives.asymmetric import rsa


# See: https://claude.ai/share/94c60538-7bd1-40e1-83aa-6ab720149e04
# To see discussion about this script and integration with maskinporten

class MaskinportenClient:
    # The private key JSON file has the same public / private key pair in test and prod for now
    def __init__(self, jwk_file_path="maskinporten_private_key.json"):
        """Initialize the Maskinporten client with JWK file path."""
        self.jwk_file_path = jwk_file_path
        self.private_key = None
        self.client_id = None
        self.kid = None
        self.load_jwk()

        # Maskinporten test environment endpoints
        self.token_endpoint = "https://test.maskinporten.no/token"
        self.audience = "https://test.maskinporten.no/"

    def load_jwk(self):
        """Load and parse the JWK private key from file."""
        try:
            with open(self.jwk_file_path, 'r') as f:
                jwk_data = json.load(f)

            # Extract key ID for later use
            self.kid = jwk_data.get('kid')

            # Convert JWK to RSA private key
            self.private_key = self._jwk_to_rsa_private_key(jwk_data)

            print(f"✅ Successfully loaded JWK with kid: {self.kid}")

        except FileNotFoundError:
            raise Exception(f"❌ JWK file not found: {self.jwk_file_path}")
        except json.JSONDecodeError:
            raise Exception(f"❌ Invalid JSON in JWK file: {self.jwk_file_path}")
        except Exception as e:
            raise Exception(f"❌ Error loading JWK: {str(e)}")

    def _jwk_to_rsa_private_key(self, jwk_data):
        """Convert JWK format to RSA private key object."""
        try:
            # Decode base64url encoded values
            def decode_base64url(data):
                # Add padding if needed
                padding = 4 - len(data) % 4
                if padding != 4:
                    data += '=' * padding
                return base64.urlsafe_b64decode(data)

            # Extract RSA components from JWK
            n = int.from_bytes(decode_base64url(jwk_data['n']), 'big')  # modulus
            e = int.from_bytes(decode_base64url(jwk_data['e']), 'big')  # public exponent
            d = int.from_bytes(decode_base64url(jwk_data['d']), 'big')  # private exponent
            p = int.from_bytes(decode_base64url(jwk_data['p']), 'big')  # prime 1
            q = int.from_bytes(decode_base64url(jwk_data['q']), 'big')  # prime 2
            dp = int.from_bytes(decode_base64url(jwk_data['dp']), 'big')  # exponent 1
            dq = int.from_bytes(decode_base64url(jwk_data['dq']), 'big')  # exponent 2
            qi = int.from_bytes(decode_base64url(jwk_data['qi']), 'big')  # coefficient

            # Create RSA private key
            private_key = rsa.RSAPrivateNumbers(
                p=p, q=q, d=d, dmp1=dp, dmq1=dq, iqmp=qi,
                public_numbers=rsa.RSAPublicNumbers(e=e, n=n)
            ).private_key()

            return private_key

        except Exception as e:
            raise Exception(f"Failed to convert JWK to RSA key: {str(e)}")

    def set_client_id(self, client_id):
        """Set the client ID (issuer) for token requests."""
        self.client_id = client_id
        print(f"✅ Client ID set to: {client_id}")

    def create_jwt_assertion(self, scope, consumer_org_no=None):
        """Create and sign a JWT assertion for Maskinporten."""
        if not self.client_id:
            raise Exception("❌ Client ID must be set before creating JWT assertion")

        now = int(time.time())

        # JWT Header - must include 'kid' as per Maskinporten requirements
        headers = {
            "alg": "RS256",
            "typ": "JWT"
        }

        # Include kid (key identifier) as required by Maskinporten
        if self.kid:
            headers["kid"] = self.kid
        else:
            raise Exception("❌ Key ID (kid) is required for Maskinporten")

        # JWT Payload
        payload = {
            "aud": self.audience,
            "iss": self.client_id,
            "scope": scope,
            "iat": now,
            "exp": now + 120,  # 2 minutes (max allowed)
            "jti": str(uuid.uuid4())
        }

        # Add consumer organization if acting on behalf of another org
        if consumer_org_no:
            payload["consumer_org"] = consumer_org_no
            print(f"🏢 Acting on behalf of organization: {consumer_org_no}")

        # Debug: Print JWT payload
        print(f"🔍 JWT payload: {json.dumps(payload, indent=2)}")

        # Sign the JWT
        try:
            token = jwt.encode(
                payload,
                self.private_key,
                algorithm="RS256",
                headers=headers
            )
            print(f"✅ Created JWT assertion for scope: {scope}")
            return token

        except Exception as e:
            raise Exception(f"❌ Failed to create JWT: {str(e)}")

    def get_access_token(self, scope, consumer_org_no=None):
        """Request an access token from Maskinporten."""
        # Create JWT assertion
        assertion = self.create_jwt_assertion(scope, consumer_org_no)

        # Prepare request
        headers = {
            "Content-Type": "application/x-www-form-urlencoded"
        }

        data = {
            "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
            "assertion": assertion
        }

        print(f"🚀 Requesting access token from: {self.token_endpoint}")

        try:
            response = requests.post(
                self.token_endpoint,
                headers=headers,
                data=data,
                timeout=30
            )

            if response.status_code == 200:
                token_data = response.json()
                print(f"✅ Successfully received access token!")
                print(f"   Token type: {token_data.get('token_type')}")
                print(f"   Expires in: {token_data.get('expires_in')} seconds")
                print(f"   Scope: {token_data.get('scope')}")
                return token_data
            else:
                print(f"❌ Token request failed with status {response.status_code}")
                print(f"   Response: {response.text}")
                return None

        except requests.RequestException as e:
            print(f"❌ Network error: {str(e)}")
            return None


def main():
    """Example usage of the Maskinporten client."""
    try:
        # Initialize client
        client = MaskinportenClient()

        # Set your client ID (you get this when you create the integration)

        # TEST: https://sjolvbetjening.test.samarbeid.digdir.no/clients/6a3e4a77-1102-4bc6-a2bd-da6e5313ad7d
        client_id = "6a3e4a77-1102-4bc6-a2bd-da6e5313ad7d"

        # PROD: https://sjolvbetjening.samarbeid.digdir.no/clients/2847bb25-8724-4da9-aa86-711913d8c8fb
        # client_id = "2847bb25-8724-4da9-aa86-711913d8c8fb"
        client.set_client_id(client_id)

        # Set the scope you want to access
        # scope = "altinn:authentication/systemuser.request.read"
        scope = "skatteetaten:formueinntekt/skattemelding"
        scope = "skatteetaten:skattemelding"

        # Optional: acting on behalf of another organization
        consumer_org = "922989451"  # org nr
        consumer_org = consumer_org if consumer_org else None

        # Get access token
        token_response = client.get_access_token(scope, consumer_org)

        if token_response:
            print("\n" + "=" * 50)
            print("ACCESS TOKEN RECEIVED:")
            print("=" * 50)
            print(f"access_token: {token_response['access_token']}")
            print("\nYou can now use this token in your API calls!")
        else:
            print("\n❌ Failed to get access token")

    except Exception as e:
        print(f"\n❌ Error: {str(e)}")


if __name__ == "__main__":
    main()
