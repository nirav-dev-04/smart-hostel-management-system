import urllib.request
import urllib.parse
import json
import time

BASE_URL = "http://localhost:9001"

def make_request(path, method="GET", data=None, token=None):
    url = f"{BASE_URL}{path}"
    headers = {
        "Content-Type": "application/json"
    }
    if token:
        headers["Authorization"] = f"Bearer {token}"
        
    req_data = None
    if data:
        req_data = json.dumps(data).encode("utf-8")
        
    req = urllib.request.Request(url, data=req_data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as res:
            resp_body = res.read().decode("utf-8")
            return res.status, json.loads(resp_body)
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode("utf-8")
        try:
            return e.code, json.loads(resp_body)
        except Exception:
            return e.code, {"success": False, "message": resp_body}
    except Exception as e:
        return 500, {"success": False, "message": str(e)}

def run_tests():
    print("=====================================================================")
    print("             SMART HOSTEL BACKEND SUITE VERIFICATION")
    print("=====================================================================")
    print()
    
    timestamp = int(time.time())
    student_email = f"student_{timestamp}@hostel.com"
    rector_email = f"rector_{timestamp}@hostel.com"
    admin_email = f"admin_{timestamp}@hostel.com"
    
    # -------------------------------------------------------------
    # 1. REGISTER STUDENT
    # -------------------------------------------------------------
    print("1. Testing Registration (Student)...")
    student_reg = {
        "name": "Test Student",
        "email": student_email,
        "password": "Password123",
        "role": "STUDENT",
        "hostelBlock": "Block A",
        "roomNumber": "101",
        "phone": "+1234567890"
    }
    status, resp = make_request("/api/auth/register", "POST", student_reg)
    if status == 200 and resp.get("success"):
        print(f"   [SUCCESS] Student registered! ID: {resp['data']['userId']}")
    else:
        print(f"   [FAIL] Status {status}: {resp}")
        return

    # -------------------------------------------------------------
    # 2. REGISTER RECTOR
    # -------------------------------------------------------------
    print("\n2. Testing Registration (Rector)...")
    rector_reg = {
        "name": "Test Rector",
        "email": rector_email,
        "password": "Password123",
        "role": "RECTOR",
        "hostelBlock": "Block A",
        "phone": "+1987654321"
    }
    status, resp = make_request("/api/auth/register", "POST", rector_reg)
    if status == 200 and resp.get("success"):
        print(f"   [SUCCESS] Rector registered! ID: {resp['data']['userId']}")
        rector_id = resp['data']['userId']
    else:
        print(f"   [FAIL] Status {status}: {resp}")
        return

    # -------------------------------------------------------------
    # 3. REGISTER ADMIN
    # -------------------------------------------------------------
    print("\n3. Testing Registration (Admin)...")
    admin_reg = {
        "name": "Test Admin",
        "email": admin_email,
        "password": "Password123",
        "role": "ADMIN",
        "phone": "+1122334455"
    }
    status, resp = make_request("/api/auth/register", "POST", admin_reg)
    if status == 200 and resp.get("success"):
        print(f"   [SUCCESS] Admin registered! ID: {resp['data']['userId']}")
    else:
        print(f"   [FAIL] Status {status}: {resp}")
        return

    # -------------------------------------------------------------
    # 4. DUPLICATE REGISTRATION VALIDATION
    # -------------------------------------------------------------
    print("\n4. Testing Duplicate Email Validation...")
    status, resp = make_request("/api/auth/register", "POST", student_reg)
    if status == 400 or not resp.get("success"):
        print(f"   [SUCCESS] Duplicate email prevented! Code: {status}, Msg: {resp.get('message')}")
    else:
        print(f"   [FAIL] Duplicate email registration allowed! Status {status}: {resp}")
        return

    # -------------------------------------------------------------
    # 5. LOGIN FLOWS & JWT GENERATION
    # -------------------------------------------------------------
    print("\n5. Testing Logins & JWT Generation...")
    
    # Student Login
    status, resp = make_request("/api/auth/login", "POST", {"email": student_email, "password": "Password123"})
    if status == 200 and resp.get("success"):
        student_token = resp["data"]["token"]
        print(f"   [SUCCESS] Student logged in! Token extracted.")
    else:
        print(f"   [FAIL] Student login failed: {resp}")
        return
        
    # Rector Login
    status, resp = make_request("/api/auth/login", "POST", {"email": rector_email, "password": "Password123"})
    if status == 200 and resp.get("success"):
        rector_token = resp["data"]["token"]
        print(f"   [SUCCESS] Rector logged in! Token extracted.")
    else:
        print(f"   [FAIL] Rector login failed: {resp}")
        return
        
    # Admin Login
    status, resp = make_request("/api/auth/login", "POST", {"email": admin_email, "password": "Password123"})
    if status == 200 and resp.get("success"):
        admin_token = resp["data"]["token"]
        print(f"   [SUCCESS] Admin logged in! Token extracted.")
    else:
        print(f"   [FAIL] Admin login failed: {resp}")
        return

    # -------------------------------------------------------------
    # 6. PROFILE RETRIEVAL (GET /api/users/me)
    # -------------------------------------------------------------
    print("\n6. Testing JWT Auth Claim Mapping (/api/users/me)...")
    status, resp = make_request("/api/users/me", "GET", token=student_token)
    if status == 200 and resp.get("success"):
        print(f"   [SUCCESS] Profile retrieved for Student: {resp['data']['email']} ({resp['data']['role']})")
    else:
        print(f"   [FAIL] Profile retrieval failed: {resp}")
        return

    # -------------------------------------------------------------
    # 7. ROLE-BASED ACCESS CONTROL (RBAC) SECURITY TEST
    # -------------------------------------------------------------
    print("\n7. Testing Role-Based Authorization Constraints...")
    # Student trying to access Admin dashboard
    status, resp = make_request("/api/admin/dashboard", "GET", token=student_token)
    if status == 403:
        print(f"   [SUCCESS] STUDENT prevented from accessing Admin routes (Forbidden 403)")
    else:
        print(f"   [FAIL] STUDENT was allowed access to Admin routes: Status {status}")
        return

    # Student trying to access Rector complaints
    status, resp = make_request("/api/rector/complaints", "GET", token=student_token)
    if status == 403:
        print(f"   [SUCCESS] STUDENT prevented from accessing Rector routes (Forbidden 403)")
    else:
        print(f"   [FAIL] STUDENT was allowed access to Rector routes: Status {status}")
        return

    # Admin accessing Admin dashboard
    status, resp = make_request("/api/admin/dashboard", "GET", token=admin_token)
    if status == 200 and resp.get("success"):
        print(f"   [SUCCESS] ADMIN successfully allowed access to Admin dashboard!")
    else:
        print(f"   [FAIL] ADMIN blocked from Admin dashboard: Status {status}, {resp}")
        return

    # -------------------------------------------------------------
    # 8. COMPLAINT WORKFLOW LIFECYCLE
    # -------------------------------------------------------------
    print("\n8. Testing End-to-End Complaint Lifecycle Workflow...")
    
    # A. Create Complaint (Student)
    complaint_data = {
        "title": "Water leakage in bathroom A-101",
        "description": "Tap in room 101 block A is leaking persistently.",
        "category": "WATER",
        "priority": "HIGH"
    }
    status, resp = make_request("/api/complaints", "POST", complaint_data, token=student_token)
    if status == 200 and resp.get("success"):
        complaint_id = resp["data"]["id"]
        print(f"   [SUCCESS] Complaint created by Student! ID: {complaint_id}, Status: {resp['data']['status']}")
    else:
        print(f"   [FAIL] Complaint creation failed: Status {status}, {resp}")
        return

    # B. Rector views assigned complaints
    status, resp = make_request("/api/rector/complaints", "GET", token=rector_token)
    if status == 200 and resp.get("success"):
        complaints_list = resp["data"]
        found = any(c["id"] == complaint_id for c in complaints_list)
        if found:
            print(f"   [SUCCESS] Rector sees the newly auto-assigned block complaint (ID: {complaint_id})!")
        else:
            print(f"   [FAIL] Complaint (ID: {complaint_id}) not assigned/found in Rector block list: {complaints_list}")
    else:
        print(f"   [FAIL] Rector complaints fetching failed: Status {status}")
        return

    # C. Rector updates complaint status to IN_PROGRESS
    status, resp = make_request(f"/api/rector/complaints/{complaint_id}/status", "PUT", {"status": "IN_PROGRESS"}, token=rector_token)
    if status == 200 and resp.get("success"):
        print(f"   [SUCCESS] Rector updated status to: {resp['data']['status']}")
    else:
        print(f"   [FAIL] Rector status update failed: Status {status}, {resp}")
        return

    # D. Rector adds resolution notes and marks as RESOLVED
    note_data = {
        "resolutionNote": "Plumber visited and replaced the damaged faucet valve. Leak stopped."
    }
    status, resp = make_request(f"/api/rector/complaints/{complaint_id}/note", "POST", note_data, token=rector_token)
    if status == 200 and resp.get("success"):
        print(f"   [SUCCESS] Rector added resolution note!")
    else:
        print(f"   [FAIL] Rector adding resolution note failed: Status {status}")
        return
        
    status, resp = make_request(f"/api/rector/complaints/{complaint_id}/status", "PUT", {"status": "RESOLVED"}, token=rector_token)
    if status == 200 and resp.get("success"):
        print(f"   [SUCCESS] Rector resolved complaint! Status: {resp['data']['status']}")
    else:
        print(f"   [FAIL] Rector resolving complaint failed: Status {status}")
        return

    # E. Student marks resolved complaint as CLOSED
    status, resp = make_request(f"/api/complaints/{complaint_id}/status", "PUT", {"status": "CLOSED"}, token=student_token)
    if status == 200 and resp.get("success"):
        print(f"   [SUCCESS] Student closed their complaint! Status: {resp['data']['status']}")
    else:
        print(f"   [FAIL] Student closing complaint failed: Status {status}, {resp}")
        return

    # -------------------------------------------------------------
    # 9. VALIDATION ERROR HANDLING
    # -------------------------------------------------------------
    print("\n9. Testing Request DTO Validation Filters...")
    bad_complaint = {
        "title": "",  # Blank title
        "category": "INVALID_CAT", # Invalid category
        "priority": "HIGH"
    }
    status, resp = make_request("/api/complaints", "POST", bad_complaint, token=student_token)
    if status == 400:
        print(f"   [SUCCESS] Validation failed correctly (Bad Request 400). Response: {resp.get('message')}")
    else:
        print(f"   [FAIL] Validation bypassed! Allowed invalid payload: Status {status}")
        return

    print("\n=====================================================================")
    print("     ALL TESTS COMPLETED SUCCESSFULLY! BACKEND STACK IS 100% HEALTHY")
    print("=====================================================================")

if __name__ == "__main__":
    run_tests()
