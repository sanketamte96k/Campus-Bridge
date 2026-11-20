package com.campusbridge.backend.config;

import com.campusbridge.backend.entity.EligibilityRuleEmbeddable;
import com.campusbridge.backend.entity.Job;
import com.campusbridge.backend.entity.StudentProfile;
import com.campusbridge.backend.entity.User;
import com.campusbridge.backend.enums.Branch;
import com.campusbridge.backend.enums.Role;
import com.campusbridge.backend.repository.JobRepository;
import com.campusbridge.backend.repository.StudentProfileRepository;
import com.campusbridge.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            StudentProfileRepository profileRepository,
            JobRepository jobRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.jobRepository = jobRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            logger.info("Database is empty. Initializing seed data...");

            // 1. Seed Recruiter
            User recruiter = new User();
            recruiter.setName("Sarah Jenkins");
            recruiter.setEmail("recruiter@campusbridge.edu");
            recruiter.setPasswordHash(passwordEncoder.encode("Password123!"));
            recruiter.setRole(Role.RECRUITER);
            User savedRecruiter = userRepository.save(recruiter);

            // 2. Seed Student
            User student = new User();
            student.setName("Alex Rivera");
            student.setEmail("student@campusbridge.edu");
            student.setPasswordHash(passwordEncoder.encode("Password123!"));
            student.setRole(Role.STUDENT);
            User savedStudent = userRepository.save(student);

            // 3. Seed Student Profile
            StudentProfile profile = new StudentProfile();
            profile.setUser(savedStudent);
            profile.setBranch(Branch.CS);
            profile.setCgpa(new BigDecimal("8.50"));
            profile.setActiveBacklogs(0);
            profile.setSkills(List.of("Java", "Spring Boot", "SQL", "React", "TypeScript", "Tailwind"));
            profileRepository.save(profile);

            // 4. Seed Jobs
            List<Job> initialJobs = List.of(
                    new Job(
                            "Java developer",
                            "Northwind Technologies",
                            "Pune",
                            6.0,
                            LocalDate.of(2026, 12, 15),
                            "Build robust enterprise backend services and scalable RESTful APIs with clean architecture.",
                            new EligibilityRuleEmbeddable(
                                    List.of(Branch.IT, Branch.CS),
                                    new BigDecimal("7.50"),
                                    0,
                                    List.of("Java", "SQL", "Spring Boot")
                            ),
                            savedRecruiter.getId()
                    ),
                    new Job(
                            "Frontend engineer",
                            "Pixelforge Labs",
                            "Bengaluru",
                            8.0,
                            LocalDate.of(2026, 12, 20),
                            "Craft responsive, user-friendly web interfaces and component systems with modern web standards.",
                            new EligibilityRuleEmbeddable(
                                    List.of(Branch.IT, Branch.CS, Branch.ENTC),
                                    new BigDecimal("7.00"),
                                    0,
                                    List.of("React", "TypeScript", "Tailwind")
                            ),
                            savedRecruiter.getId()
                    ),
                    new Job(
                            "Software trainee",
                            "Brightline Systems",
                            "Mumbai",
                            4.5,
                            LocalDate.of(2027, 1, 5),
                            "Kickstart your engineering career through practical software engineering training and team mentorship.",
                            new EligibilityRuleEmbeddable(
                                    List.of(Branch.IT, Branch.CS, Branch.ENTC, Branch.MECH),
                                    new BigDecimal("6.00"),
                                    1,
                                    List.of("Java", "DSA")
                            ),
                            savedRecruiter.getId()
                    ),
                    new Job(
                            "Data analyst",
                            "Quantive Analytics",
                            "Hyderabad",
                            7.0,
                            LocalDate.of(2027, 1, 10),
                            "Transform complex institutional datasets into actionable insights and automated reporting dashboards.",
                            new EligibilityRuleEmbeddable(
                                    List.of(Branch.IT, Branch.CS),
                                    new BigDecimal("7.50"),
                                    0,
                                    List.of("Python", "SQL")
                            ),
                            savedRecruiter.getId()
                    ),
                    new Job(
                            "Backend engineer",
                            "Cloudmesh",
                            "Pune",
                            10.0,
                            LocalDate.of(2027, 1, 20),
                            "Architect distributed backend systems and deploy containerized microservices to cloud infrastructure.",
                            new EligibilityRuleEmbeddable(
                                    List.of(Branch.IT, Branch.CS),
                                    new BigDecimal("8.00"),
                                    0,
                                    List.of("Java", "Spring Boot", "MySQL", "Docker")
                            ),
                            savedRecruiter.getId()
                    )
            );

            jobRepository.saveAll(initialJobs);
            logger.info("Seed data initialized successfully with {} jobs, 1 recruiter, and 1 student.", initialJobs.size());
        }
    }
}
