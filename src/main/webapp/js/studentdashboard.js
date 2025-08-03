document.addEventListener("DOMContentLoaded", function () {
    fetchStudentData();
    fetchAnnouncements();
});

function fetchStudentData() {
    fetch("../StudentDashboardServlet")
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                console.error("Error fetching student data:", data.error);
                return;
            }

            document.getElementById("student-name").textContent = data.firstName + " " + data.lastName;
            document.getElementById("student-email").textContent = data.email;
            document.getElementById("student-course").textContent = data.course;
            const enrollmentDate = data.enrollmentDate ? data.enrollmentDate.split(" ")[0] : "Not available";
            const dob = data.dob ? data.dob.split(" ")[0] : "Not available";
            document.getElementById("student-dob").textContent = dob;
            document.getElementById("student-enrollment").textContent = enrollmentDate;
            
            // Later we will fetch and display profile picture
        })
        .catch(error => console.error("Error:", error));
}

function fetchAnnouncements() {
    fetch("../AnnouncementServlet")
        .then(response => response.json())
        .then(announcements => {
            let announcementsHtml = "";
            announcements.forEach(announcement => {
                announcementsHtml += `
                    <div class="announcement-item">
                        <p>${announcement.text}</p>
                        <small>${announcement.date}</small>
                    </div>
                `;
            });
            document.getElementById("announcements").innerHTML = announcementsHtml;
        })
        .catch(error => console.error("Error fetching announcements:", error));
}