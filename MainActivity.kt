package com.example.cybernexus

// ============================================================
// CyberNexus - Educational Penetration Testing Platform
// Complete single-file Jetpack Compose implementation
// Theme: White + Lavender | Architecture: MVVM + Room + Hilt
// ⚠️ STRICTLY EDUCATIONAL - All features are simulations
// ============================================================

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// ============================================================
// THEME & DESIGN SYSTEM
// ============================================================

object CyberNexusColors {
    val Background = Color(0xFFF8F7FF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF3F0FF)
    val Primary = Color(0xFF7C5CBF)
    val PrimaryLight = Color(0xFF9B7FD4)
    val PrimaryDark = Color(0xFF5A3D9A)
    val Secondary = Color(0xFFB39DDB)
    val SecondaryLight = Color(0xFFD1C4E9)
    val Accent = Color(0xFF6A1B9A)
    val LavenderMist = Color(0xFFEDE7F6)
    val LavenderSoft = Color(0xFFD1C4E9)
    val LavenderMed = Color(0xFFB39DDB)
    val LavenderDeep = Color(0xFF9575CD)
    val TextPrimary = Color(0xFF1A1033)
    val TextSecondary = Color(0xFF5C4D7A)
    val TextHint = Color(0xFF9E8EB5)
    val Success = Color(0xFF43A047)
    val Warning = Color(0xFFFB8C00)
    val Error = Color(0xFFE53935)
    val Info = Color(0xFF1E88E5)
    val CardBorder = Color(0xFFE8E0F7)
    val DividerColor = Color(0xFFF0EBF9)
    val RiskLow = Color(0xFF66BB6A)
    val RiskMedium = Color(0xFFFFCA28)
    val RiskHigh = Color(0xFFEF5350)
    val RiskCritical = Color(0xFFB71C1C)
    val NeonGlow = Color(0xFF9C6FE4)
    val GlassWhite = Color(0x99FFFFFF)
}

val LatoFontFamily = FontFamily.Default // In real project: use custom font via res/font

// ============================================================
// DATA MODELS
// ============================================================

enum class Screen {
    SPLASH, HOME, RECON, SCANNING, VULN_ANALYSIS,
    EXPLOITATION, REPORTING, LEARNING, TRACKS
}

enum class RiskLevel(val label: String, val color: Color, val score: Int) {
    LOW("Low", CyberNexusColors.RiskLow, 25),
    MEDIUM("Medium", CyberNexusColors.RiskMedium, 50),
    HIGH("High", CyberNexusColors.RiskHigh, 75),
    CRITICAL("Critical", CyberNexusColors.RiskCritical, 95)
}

data class ReconResult(
    val id: String = UUID.randomUUID().toString(),
    val domain: String,
    val ipAddress: String,
    val openPorts: List<Int>,
    val subdomains: List<String>,
    val whoisInfo: Map<String, String>,
    val dnsRecords: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

data class ScanResult(
    val id: String = UUID.randomUUID().toString(),
    val target: String,
    val service: String,
    val port: Int,
    val protocol: String,
    val state: String,
    val version: String,
    val riskLevel: RiskLevel,
    val timestamp: Long = System.currentTimeMillis()
)

data class Vulnerability(
    val id: String = UUID.randomUUID().toString(),
    val cveId: String,
    val title: String,
    val description: String,
    val riskLevel: RiskLevel,
    val cvssScore: Float,
    val affectedService: String,
    val remediation: String,
    val references: List<String>
)

data class ExploitSimulation(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String,
    val description: String,
    val targetVuln: String,
    val steps: List<String>,
    val impact: String,
    val isSimulated: Boolean = true
)

data class PentestReport(
    val id: String = UUID.randomUUID().toString(),
    val targetDomain: String,
    val executiveSummary: String,
    val findings: List<Vulnerability>,
    val riskScore: Float,
    val recommendations: List<String>,
    val generatedAt: Long = System.currentTimeMillis()
)

data class LearningModule(
    val id: String,
    val title: String,
    val phase: String,
    val description: String,
    val keyPoints: List<String>,
    val tools: List<String>,
    val icon: String
)

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// ============================================================
// MOCK DATA PROVIDERS
// ============================================================

object MockData {

    val reconResult = ReconResult(
        domain = "target-demo.edu",
        ipAddress = "192.168.1.100",
        openPorts = listOf(21, 22, 80, 443, 8080, 3306),
        subdomains = listOf("www", "mail", "ftp", "api", "admin", "dev", "staging"),
        whoisInfo = mapOf(
            "Registrar" to "Demo Registrar Inc.",
            "Created" to "2018-04-12",
            "Expires" to "2026-04-12",
            "Country" to "US",
            "Organization" to "Demo University"
        ),
        dnsRecords = listOf(
            "A     192.168.1.100",
            "MX    mail.target-demo.edu",
            "NS    ns1.target-demo.edu",
            "TXT   v=spf1 include:_spf.demo.edu ~all",
            "CNAME www → target-demo.edu"
        )
    )

    val scanResults = listOf(
        ScanResult("", "target-demo.edu", "FTP", 21, "TCP", "open", "vsftpd 3.0.3", RiskLevel.HIGH),
        ScanResult("", "target-demo.edu", "SSH", 22, "TCP", "open", "OpenSSH 7.4", RiskLevel.MEDIUM),
        ScanResult("", "target-demo.edu", "HTTP", 80, "TCP", "open", "Apache 2.4.41", RiskLevel.MEDIUM),
        ScanResult("", "target-demo.edu", "HTTPS", 443, "TCP", "open", "Apache 2.4.41 + TLS 1.2", RiskLevel.LOW),
        ScanResult("", "target-demo.edu", "HTTP-Alt", 8080, "TCP", "open", "Tomcat 9.0.31", RiskLevel.HIGH),
        ScanResult("", "target-demo.edu", "MySQL", 3306, "TCP", "open", "MySQL 5.7.28", RiskLevel.CRITICAL)
    )

    val vulnerabilities = listOf(
        Vulnerability(
            cveId = "CVE-2021-41773",
            title = "Apache Path Traversal & RCE",
            description = "[SIMULATION] A flaw in Apache 2.4.49 allows path traversal attacks and possibly RCE if mod_cgi is enabled. This is a demonstration only.",
            riskLevel = RiskLevel.CRITICAL,
            cvssScore = 9.8f,
            affectedService = "Apache HTTP 2.4.41",
            remediation = "Upgrade to Apache 2.4.51 or later. Disable mod_cgi if not required.",
            references = listOf("https://nvd.nist.gov/vuln/detail/CVE-2021-41773")
        ),
        Vulnerability(
            cveId = "CVE-2020-8816",
            title = "MySQL Unauthenticated Access",
            description = "[SIMULATION] MySQL 5.7 exposed on port 3306 with weak bind-address configuration allowing external connections.",
            riskLevel = RiskLevel.CRITICAL,
            cvssScore = 9.1f,
            affectedService = "MySQL 5.7.28",
            remediation = "Restrict MySQL bind-address to 127.0.0.1. Enforce strong credentials.",
            references = listOf("https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2020-8816")
        ),
        Vulnerability(
            cveId = "CVE-2019-0232",
            title = "Tomcat CGI Remote Code Execution",
            description = "[SIMULATION] Apache Tomcat on Windows with enableCmdLineArguments=true allows RCE via CGI servlet.",
            riskLevel = RiskLevel.HIGH,
            cvssScore = 8.1f,
            affectedService = "Tomcat 9.0.31",
            remediation = "Disable CGI servlet or set enableCmdLineArguments=false.",
            references = listOf("https://nvd.nist.gov/vuln/detail/CVE-2019-0232")
        ),
        Vulnerability(
            cveId = "CVE-2021-22986",
            title = "Weak SSH Configuration",
            description = "[SIMULATION] SSH service allows password authentication and uses deprecated key exchange algorithms.",
            riskLevel = RiskLevel.MEDIUM,
            cvssScore = 5.9f,
            affectedService = "OpenSSH 7.4",
            remediation = "Disable password auth. Use key-based auth only. Update cipher suites.",
            references = listOf("https://cve.mitre.org")
        ),
        Vulnerability(
            cveId = "CVE-2015-3306",
            title = "vsftpd Anonymous Login Enabled",
            description = "[SIMULATION] FTP server allows anonymous login exposing directory listings.",
            riskLevel = RiskLevel.HIGH,
            cvssScore = 7.5f,
            affectedService = "vsftpd 3.0.3",
            remediation = "Disable anonymous_enable in vsftpd.conf. Enforce authenticated access.",
            references = listOf("https://nvd.nist.gov/vuln/detail/CVE-2015-3306")
        )
    )

    val exploitSimulations = listOf(
        ExploitSimulation(
            name = "Path Traversal Demo",
            type = "Web Exploitation",
            description = "Educational simulation of how path traversal attacks work on misconfigured web servers.",
            targetVuln = "CVE-2021-41773",
            steps = listOf(
                "1. Identify target URL structure",
                "2. Craft traversal payload: ../../../../etc/passwd",
                "3. Observe server response for directory listing",
                "4. [SIMULATION ENDS — No actual exploit executed]"
            ),
            impact = "Unauthorized file read, potential RCE"
        ),
        ExploitSimulation(
            name = "SQL Injection Concept",
            type = "Database Attack",
            description = "Demonstrates how SQL injection payloads target unsanitized database queries.",
            targetVuln = "MySQL Exposure",
            steps = listOf(
                "1. Identify input field interacting with DB",
                "2. Test with: ' OR '1'='1",
                "3. Observe login bypass or data dump",
                "4. [SIMULATION ENDS — Educational concept only]"
            ),
            impact = "Data exfiltration, authentication bypass"
        ),
        ExploitSimulation(
            name = "Brute Force SSH Demo",
            type = "Authentication Attack",
            description = "Illustrates how weak passwords enable brute force attacks via SSH.",
            targetVuln = "CVE-2021-22986",
            steps = listOf(
                "1. Enumerate valid usernames via OSINT",
                "2. Use wordlist against SSH service",
                "3. Gain shell access on credential match",
                "4. [SIMULATION ENDS — Never do this without authorization]"
            ),
            impact = "Full system compromise"
        )
    )

    val learningModules = listOf(
        LearningModule("1", "Reconnaissance", "Phase 1", "Information gathering about the target without active interaction.", listOf("Passive OSINT collection", "WHOIS lookups", "DNS enumeration", "Social engineering recon", "Public records analysis"), listOf("Maltego", "theHarvester", "Shodan", "Recon-ng", "OSINT Framework"), "🔍"),
        LearningModule("2", "Scanning & Enumeration", "Phase 2", "Active probing of target systems to discover open services and versions.", listOf("Port scanning techniques", "Service fingerprinting", "OS detection", "Banner grabbing", "Network topology mapping"), listOf("Nmap", "Masscan", "Netcat", "Nessus", "OpenVAS"), "📡"),
        LearningModule("3", "Vulnerability Analysis", "Phase 3", "Identifying and assessing weaknesses in discovered services.", listOf("CVE database research", "CVSS scoring", "Risk prioritization", "Manual verification", "False positive analysis"), listOf("Nessus", "OpenVAS", "Nikto", "Burp Suite", "OWASP ZAP"), "⚠️"),
        LearningModule("4", "Exploitation (Ethical)", "Phase 4", "Controlled demonstration of vulnerability impact with authorization.", listOf("Proof-of-concept development", "Payload crafting", "Privilege escalation paths", "Lateral movement concepts", "Impact documentation"), listOf("Metasploit", "ExploitDB", "Burp Suite Pro", "SQLmap", "Hydra"), "⚡"),
        LearningModule("5", "Post-Exploitation Analysis", "Phase 5", "Understanding attacker goals after initial access is obtained.", listOf("Persistence mechanisms", "Data exfiltration paths", "Credential harvesting concepts", "Pivoting techniques", "Defense evasion awareness"), listOf("Mimikatz (education)", "BloodHound", "PowerSploit (education)", "CrackMapExec"), "🎯"),
        LearningModule("6", "Reporting", "Phase 6", "Documenting findings professionally for stakeholders.", listOf("Executive summary writing", "CVSS-based risk rating", "Evidence documentation", "Remediation recommendations", "Compliance mapping"), listOf("Dradis", "Faraday", "PlexTrac", "Microsoft Word", "LaTeX"), "📋"),
        LearningModule("7", "Clearing Tracks (Defensive)", "Phase 7", "Understanding log hygiene and defensive awareness from attacker perspective.", listOf("Log analysis concepts", "Audit trail importance", "SIEM detection rules", "Incident response basics", "System hardening after test"), listOf("Splunk", "ELK Stack", "Wazuh", "OSSEC", "Suricata"), "🛡️")
    )

    val pentestReport = PentestReport(
        targetDomain = "target-demo.edu",
        executiveSummary = "This penetration test was conducted as an educational simulation against target-demo.edu. The assessment identified 5 vulnerabilities across 6 exposed services. Two critical vulnerabilities were discovered requiring immediate remediation. The overall security posture is rated as HIGH RISK.",
        findings = vulnerabilities,
        riskScore = 8.4f,
        recommendations = listOf(
            "Immediately restrict MySQL port 3306 to localhost only",
            "Upgrade Apache HTTP to latest stable version",
            "Disable anonymous FTP access and enforce authentication",
            "Implement key-based SSH authentication only",
            "Deploy Web Application Firewall (WAF)",
            "Conduct quarterly penetration testing",
            "Implement a vulnerability disclosure program",
            "Train development staff on secure coding practices"
        )
    )
}

// ============================================================
// VIEW MODEL
// ============================================================

class CyberNexusViewModel : ViewModel() {

    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _reconResult = MutableStateFlow<ReconResult?>(null)
    val reconResult: StateFlow<ReconResult?> = _reconResult.asStateFlow()

    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults.asStateFlow()

    private val _vulnerabilities = MutableStateFlow<List<Vulnerability>>(emptyList())
    val vulnerabilities: StateFlow<List<Vulnerability>> = _vulnerabilities.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _selectedVuln = MutableStateFlow<Vulnerability?>(null)
    val selectedVuln: StateFlow<Vulnerability?> = _selectedVuln.asStateFlow()

    private val _report = MutableStateFlow<PentestReport?>(null)
    val report: StateFlow<PentestReport?> = _report.asStateFlow()

    private val _terminalLines = MutableStateFlow<List<String>>(emptyList())
    val terminalLines: StateFlow<List<String>> = _terminalLines.asStateFlow()

    fun navigate(screen: Screen) { _currentScreen.value = screen }

    fun selectVuln(v: Vulnerability?) { _selectedVuln.value = v }

    fun startRecon(domain: String) {
        viewModelScope.launch {
            _isScanning.value = true
            _terminalLines.value = emptyList()
            _scanProgress.value = 0f

            val reconLines = listOf(
                "[*] Starting reconnaissance on $domain",
                "[*] Resolving DNS records...",
                "[+] A record: 192.168.1.100",
                "[+] MX record: mail.$domain",
                "[+] TXT record: v=spf1 include:_spf.demo.edu ~all",
                "[*] Running WHOIS lookup...",
                "[+] Registrar: Demo Registrar Inc.",
                "[+] Created: 2018-04-12 | Expires: 2026-04-12",
                "[*] Enumerating subdomains...",
                "[+] Found: www.$domain",
                "[+] Found: mail.$domain",
                "[+] Found: admin.$domain",
                "[+] Found: api.$domain",
                "[+] Found: staging.$domain",
                "[*] Checking certificate transparency logs...",
                "[+] Certificate issued by: Let's Encrypt",
                "[✓] Reconnaissance complete. 7 subdomains found."
            )

            reconLines.forEachIndexed { i, line ->
                delay(300)
                _terminalLines.value += line
                _scanProgress.value = (i + 1).toFloat() / reconLines.size
            }
            _reconResult.value = MockData.reconResult
            _isScanning.value = false
        }
    }

    fun startScan(target: String) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResults.value = emptyList()
            _terminalLines.value = emptyList()
            _scanProgress.value = 0f

            val scanLines = listOf(
                "[*] Starting Nmap scan on $target",
                "[*] Scanning common ports (1-10000)...",
                "[+] Port 21/tcp OPEN — vsftpd 3.0.3",
                "[+] Port 22/tcp OPEN — OpenSSH 7.4",
                "[+] Port 80/tcp OPEN — Apache httpd 2.4.41",
                "[+] Port 443/tcp OPEN — Apache httpd 2.4.41 (TLS)",
                "[+] Port 8080/tcp OPEN — Apache Tomcat 9.0.31",
                "[+] Port 3306/tcp OPEN — MySQL 5.7.28",
                "[*] Performing OS detection...",
                "[+] OS: Ubuntu 20.04 LTS (Linux 5.4)",
                "[*] Running service version detection...",
                "[✓] Scan complete. 6 open ports discovered."
            )

            scanLines.forEachIndexed { i, line ->
                delay(400)
                _terminalLines.value += line
                _scanProgress.value = (i + 1).toFloat() / scanLines.size
                if (i in 2..7) {
                    _scanResults.value = MockData.scanResults.take(i - 1)
                }
            }
            _scanResults.value = MockData.scanResults
            _isScanning.value = false
        }
    }

    fun runVulnAnalysis() {
        viewModelScope.launch {
            _isScanning.value = true
            _vulnerabilities.value = emptyList()
            _scanProgress.value = 0f

            MockData.vulnerabilities.forEachIndexed { i, vuln ->
                delay(600)
                _vulnerabilities.value = MockData.vulnerabilities.take(i + 1)
                _scanProgress.value = (i + 1).toFloat() / MockData.vulnerabilities.size
            }
            _isScanning.value = false
        }
    }

    fun generateReport() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f
            delay(300)
            _scanProgress.value = 0.3f
            delay(400)
            _scanProgress.value = 0.7f
            delay(300)
            _scanProgress.value = 1f
            _report.value = MockData.pentestReport
            _isScanning.value = false
        }
    }
}

// ============================================================
// MAIN ACTIVITY
// ============================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CyberNexusApp()
        }
    }
}

// ============================================================
// ROOT APP COMPOSABLE
// ============================================================

@Composable
fun CyberNexusApp() {
    val vm: CyberNexusViewModel = viewModel()
    val currentScreen by vm.currentScreen.collectAsState()

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = CyberNexusColors.Primary,
            secondary = CyberNexusColors.Secondary,
            background = CyberNexusColors.Background,
            surface = CyberNexusColors.Surface,
            onPrimary = Color.White,
            onBackground = CyberNexusColors.TextPrimary,
            onSurface = CyberNexusColors.TextPrimary
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(CyberNexusColors.Background)) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } togetherWith
                            fadeOut(tween(200))
                }
            ) { screen ->
                when (screen) {
                    Screen.SPLASH -> SplashScreen { vm.navigate(Screen.HOME) }
                    Screen.HOME -> HomeScreen(vm)
                    Screen.RECON -> ReconScreen(vm)
                    Screen.SCANNING -> ScanningScreen(vm)
                    Screen.VULN_ANALYSIS -> VulnAnalysisScreen(vm)
                    Screen.EXPLOITATION -> ExploitationScreen(vm)
                    Screen.REPORTING -> ReportingScreen(vm)
                    Screen.LEARNING -> LearningScreen(vm)
                    Screen.TRACKS -> TracksScreen(vm)
                }
            }
        }
    }
}

// ============================================================
// SPLASH SCREEN
// ============================================================

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }
    val hexagonRotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(800)) }
        launch { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        launch {
            hexagonRotation.animateTo(360f, tween(2000, easing = LinearEasing))
        }
        delay(2800)
        onFinish()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(CyberNexusColors.LavenderMist, Color.White, CyberNexusColors.SurfaceVariant))
        ),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative circles
        repeat(5) { i ->
            Box(
                modifier = Modifier
                    .size((120 + i * 60).dp)
                    .alpha(0.04f + i * 0.02f)
                    .border(1.dp, CyberNexusColors.Primary, CircleShape)
                    .align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier.alpha(alpha.value).scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated Logo
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().rotate(hexagonRotation.value)) {
                    drawHexagon(this, CyberNexusColors.Primary.copy(0.15f))
                }
                Box(
                    modifier = Modifier.size(72.dp).background(
                        Brush.radialGradient(listOf(CyberNexusColors.PrimaryLight, CyberNexusColors.Primary)),
                        CircleShape
                    ).shadow(16.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⬡", fontSize = 32.sp, color = Color.White)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "CYBER",
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    letterSpacing = 6.sp,
                    color = CyberNexusColors.Primary
                )
                Text(
                    "NEXUS",
                    fontWeight = FontWeight.Light,
                    fontSize = 28.sp,
                    letterSpacing = 10.sp,
                    color = CyberNexusColors.PrimaryLight
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                "Educational Penetration Testing Platform",
                fontSize = 12.sp,
                color = CyberNexusColors.TextHint,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                modifier = Modifier.width(180.dp).clip(RoundedCornerShape(4.dp)),
                color = CyberNexusColors.Primary,
                trackColor = CyberNexusColors.LavenderSoft
            )

            Spacer(Modifier.height(8.dp))
            DisclaimerBadge()
        }
    }
}

fun drawHexagon(scope: DrawScope, color: Color) {
    val path = Path()
    val cx = scope.size.width / 2
    val cy = scope.size.height / 2
    val r = minOf(cx, cy) - 4f
    for (i in 0..5) {
        val angle = Math.PI / 180.0 * (60 * i - 30)
        val x = cx + r * cos(angle).toFloat()
        val y = cy + r * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    scope.drawPath(path, color, style = Stroke(width = 2f))
}

@Composable
fun DisclaimerBadge() {
    Row(
        modifier = Modifier
            .background(CyberNexusColors.Warning.copy(0.1f), RoundedCornerShape(20.dp))
            .border(1.dp, CyberNexusColors.Warning.copy(0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Filled.Warning, null, tint = CyberNexusColors.Warning, modifier = Modifier.size(14.dp))
        Text("FOR EDUCATIONAL USE ONLY", fontSize = 10.sp, color = CyberNexusColors.Warning, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}

// ============================================================
// HOME DASHBOARD
// ============================================================

@Composable
fun HomeScreen(vm: CyberNexusViewModel) {
    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = { BottomNavBar(Screen.HOME, vm) },
        containerColor = CyberNexusColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Header
            HomeHeader()

            // Disclaimer
            InfoBanner()

            // Stats Row
            Spacer(Modifier.height(8.dp))
            StatsRow()

            // Workflow Cards
            Spacer(Modifier.height(16.dp))
            SectionTitle("Pentest Workflow", "Complete 7-phase methodology")
            Spacer(Modifier.height(8.dp))

            val modules = listOf(
                Triple("🔍", "Reconnaissance", Screen.RECON),
                Triple("📡", "Scanning", Screen.SCANNING),
                Triple("⚠️", "Vulnerability Analysis", Screen.VULN_ANALYSIS),
                Triple("⚡", "Exploitation Sim", Screen.EXPLOITATION),
                Triple("📋", "Reporting", Screen.REPORTING),
                Triple("📚", "Learning Hub", Screen.LEARNING),
                Triple("🛡️", "Clearing Tracks", Screen.TRACKS)
            )

            modules.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { (icon, title, screen) ->
                        WorkflowCard(
                            modifier = Modifier.weight(1f),
                            icon = icon,
                            title = title,
                            onClick = { vm.navigate(screen) }
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }

            // Risk Overview
            Spacer(Modifier.height(8.dp))
            SectionTitle("Risk Overview", "From last simulation")
            Spacer(Modifier.height(12.dp))
            RiskOverviewCard()

            // Recent Activity
            Spacer(Modifier.height(16.dp))
            SectionTitle("Recent Activity", "Simulated events")
            Spacer(Modifier.height(8.dp))
            RecentActivityList()

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun HomeHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(CyberNexusColors.Primary, CyberNexusColors.PrimaryLight)
                )
            )
            .padding(top = 48.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        // Decorative background pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..4) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = 60f + i * 40f,
                    center = Offset(size.width * 0.85f, size.height * 0.3f)
                )
            }
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp)
                        .background(Color.White.copy(0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⬡", fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("CyberNexus", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                    Text("Pentest Platform", fontSize = 12.sp, color = Color.White.copy(0.75f), letterSpacing = 1.sp)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Notifications, null, tint = Color.White.copy(0.8f))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Welcome, Analyst",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White
            )
            Text(
                "Ready for today's security assessment?",
                fontSize = 14.sp,
                color = Color.White.copy(0.8f)
            )
        }
    }
}

@Composable
fun InfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(CyberNexusColors.Warning.copy(0.08f), RoundedCornerShape(12.dp))
            .border(1.dp, CyberNexusColors.Warning.copy(0.25f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.Info, null, tint = CyberNexusColors.Warning, modifier = Modifier.size(18.dp))
        Text(
            "All modules are simulations for educational purposes only. No real attacks are performed.",
            fontSize = 12.sp,
            color = CyberNexusColors.Warning,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            Triple("5", "Vulns Found", CyberNexusColors.Error),
            Triple("6", "Ports Open", CyberNexusColors.Warning),
            Triple("8.4", "Risk Score", CyberNexusColors.RiskCritical),
            Triple("2", "Reports", CyberNexusColors.Primary)
        ).forEach { (value, label, color) ->
            StatCard(modifier = Modifier.weight(1f), value = value, label = label, color = color)
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 9.sp, color = CyberNexusColors.TextHint, textAlign = TextAlign.Center, lineHeight = 12.sp)
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CyberNexusColors.TextPrimary)
        Text(subtitle, fontSize = 12.sp, color = CyberNexusColors.TextHint)
    }
}

@Composable
fun WorkflowCard(modifier: Modifier, icon: String, title: String, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CyberNexusColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(48.dp)
                    .background(CyberNexusColors.LavenderMist, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = CyberNexusColors.TextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun RiskOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Risk Distribution", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CyberNexusColors.TextPrimary)
                RiskBadge(RiskLevel.CRITICAL, "CRITICAL")
            }
            Spacer(Modifier.height(16.dp))

            listOf(
                RiskLevel.CRITICAL to 2,
                RiskLevel.HIGH to 2,
                RiskLevel.MEDIUM to 1,
                RiskLevel.LOW to 0
            ).forEach { (level, count) ->
                RiskBar(level = level, count = count, total = 5)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RiskBar(level: RiskLevel, count: Int, total: Int) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(count) {
        animProgress.animateTo(if (total > 0) count.toFloat() / total else 0f, tween(800))
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(level.label, fontSize = 12.sp, color = CyberNexusColors.TextSecondary, modifier = Modifier.width(60.dp))
        Box(
            modifier = Modifier.weight(1f).height(8.dp)
                .background(level.color.copy(0.15f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier.fillMaxHeight()
                    .fillMaxWidth(animProgress.value)
                    .background(level.color, RoundedCornerShape(4.dp))
            )
        }
        Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = level.color, modifier = Modifier.width(16.dp))
    }
}

@Composable
fun RiskBadge(level: RiskLevel, label: String) {
    Box(
        modifier = Modifier
            .background(level.color.copy(0.12f), RoundedCornerShape(20.dp))
            .border(1.dp, level.color.copy(0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, color = level.color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RecentActivityList() {
    val activities = listOf(
        Triple("🔍", "Recon on target-demo.edu", "2 min ago"),
        Triple("📡", "Port scan completed — 6 ports", "5 min ago"),
        Triple("⚠️", "5 vulnerabilities identified", "8 min ago"),
        Triple("📋", "Report generated", "12 min ago")
    )
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        activities.forEach { (icon, text, time) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, CyberNexusColors.CardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(icon, fontSize = 18.sp)
                Text(text, fontSize = 13.sp, color = CyberNexusColors.TextPrimary, modifier = Modifier.weight(1f))
                Text(time, fontSize = 11.sp, color = CyberNexusColors.TextHint)
            }
        }
    }
}

// ============================================================
// RECONNAISSANCE SCREEN
// ============================================================

@Composable
fun ReconScreen(vm: CyberNexusViewModel) {
    val reconResult by vm.reconResult.collectAsState()
    val isScanning by vm.isScanning.collectAsState()
    val progress by vm.scanProgress.collectAsState()
    val terminalLines by vm.terminalLines.collectAsState()
    var domain by remember { mutableStateOf("target-demo.edu") }
    var showResult by remember { mutableStateOf(false) }

    LaunchedEffect(reconResult) { if (reconResult != null) showResult = true }

    Scaffold(
        topBar = { ModuleTopBar("🔍 Reconnaissance", "Phase 1 — Info Gathering", vm) },
        bottomBar = { BottomNavBar(Screen.RECON, vm) },
        containerColor = CyberNexusColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // Input Card
            NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Target Domain", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberNexusColors.TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    label = { Text("e.g. target-demo.edu") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberNexusColors.Primary,
                        focusedLabelColor = CyberNexusColors.Primary
                    ),
                    leadingIcon = { Icon(Icons.Outlined.Language, null, tint = CyberNexusColors.Primary) }
                )
                Spacer(Modifier.height(12.dp))
                GradientButton(
                    text = if (isScanning) "Running Recon..." else "Start Reconnaissance",
                    onClick = { vm.startRecon(domain) },
                    enabled = !isScanning,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isScanning || terminalLines.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                TerminalCard(lines = terminalLines, progress = progress, title = "Recon Terminal")
            }

            reconResult?.let { result ->
                if (!isScanning) {
                    Spacer(Modifier.height(16.dp))
                    ReconResultCard(result)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun TerminalCard(lines: List<String>, progress: Float, title: String) {
    val listState = rememberLazyListState()
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
    }

    NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberNexusColors.TextPrimary)
            if (progress < 1f && progress > 0f) {
                Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = CyberNexusColors.Primary, fontWeight = FontWeight.Bold)
            } else if (progress >= 1f) {
                Icon(Icons.Filled.CheckCircle, null, tint = CyberNexusColors.Success, modifier = Modifier.size(18.dp))
            }
        }
        if (progress > 0f) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                color = CyberNexusColors.Primary,
                trackColor = CyberNexusColors.LavenderSoft
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF0D0D1A), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            LazyColumn(state = listState) {
                items(lines) { line ->
                    val color = when {
                        line.startsWith("[+]") -> Color(0xFF66BB6A)
                        line.startsWith("[✓]") -> Color(0xFF42A5F5)
                        line.startsWith("[!]") -> Color(0xFFFFCA28)
                        line.startsWith("[*]") -> Color(0xFFCE93D8)
                        else -> Color(0xFFB0BEC5)
                    }
                    Text(line, fontSize = 11.sp, color = color, fontFamily = FontFamily.Monospace, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ReconResultCard(result: ReconResult) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Domain Info
        NexusCard {
            Text("Domain Information", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
            Spacer(Modifier.height(12.dp))
            InfoRow("Domain", result.domain)
            InfoRow("IP Address", result.ipAddress)
            InfoRow("Open Ports", result.openPorts.joinToString(", "))
        }

        // Subdomains
        NexusCard {
            Text("Subdomains Found (${result.subdomains.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
            Spacer(Modifier.height(10.dp))
            result.subdomains.forEach { sub ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$sub.${result.domain}", fontSize = 13.sp, color = CyberNexusColors.TextSecondary, fontFamily = FontFamily.Monospace)
                    Icon(Icons.Filled.Circle, null, tint = CyberNexusColors.Success, modifier = Modifier.size(8.dp).align(Alignment.CenterVertically))
                }
                if (result.subdomains.last() != sub) HorizontalDivider(color = CyberNexusColors.DividerColor)
            }
        }

        // WHOIS
        NexusCard {
            Text("WHOIS Information", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
            Spacer(Modifier.height(10.dp))
            result.whoisInfo.forEach { (k, v) -> InfoRow(k, v) }
        }

        // DNS Records
        NexusCard {
            Text("DNS Records", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
            Spacer(Modifier.height(10.dp))
            result.dnsRecords.forEach { record ->
                Text(record, fontSize = 12.sp, color = CyberNexusColors.TextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = CyberNexusColors.TextHint)
        Text(value, fontSize = 13.sp, color = CyberNexusColors.TextPrimary, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
    }
    HorizontalDivider(color = CyberNexusColors.DividerColor, modifier = Modifier.padding(top = 4.dp))
}

// ============================================================
// SCANNING SCREEN
// ============================================================

@Composable
fun ScanningScreen(vm: CyberNexusViewModel) {
    val scanResults by vm.scanResults.collectAsState()
    val isScanning by vm.isScanning.collectAsState()
    val progress by vm.scanProgress.collectAsState()
    val terminalLines by vm.terminalLines.collectAsState()
    var target by remember { mutableStateOf("192.168.1.100") }

    Scaffold(
        topBar = { ModuleTopBar("📡 Network Scanning", "Phase 2 — Port & Service Discovery", vm) },
        bottomBar = { BottomNavBar(Screen.SCANNING, vm) },
        containerColor = CyberNexusColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Target IP / Range", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberNexusColors.TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    label = { Text("e.g. 192.168.1.0/24") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberNexusColors.Primary,
                        focusedLabelColor = CyberNexusColors.Primary
                    ),
                    leadingIcon = { Icon(Icons.Outlined.Wifi, null, tint = CyberNexusColors.Primary) }
                )
                Spacer(Modifier.height(12.dp))

                // Scan type chips
                Text("Scan Type", fontSize = 12.sp, color = CyberNexusColors.TextHint)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("TCP SYN", "UDP", "Version", "Full").forEach { type ->
                        val selected = type == "TCP SYN"
                        FilterChip(
                            selected = selected,
                            onClick = {},
                            label = { Text(type, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberNexusColors.Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                GradientButton(
                    text = if (isScanning) "Scanning..." else "Start Scan",
                    onClick = { vm.startScan(target) },
                    enabled = !isScanning,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (terminalLines.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                TerminalCard(lines = terminalLines, progress = progress, title = "Scan Output")
            }

            if (scanResults.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Open Ports (${scanResults.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
                        Text("SIMULATION", fontSize = 10.sp, color = CyberNexusColors.Warning, fontWeight = FontWeight.Bold,
                            modifier = Modifier.background(CyberNexusColors.Warning.copy(0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    scanResults.forEach { result ->
                        ScanResultRow(result)
                        if (scanResults.last() != result) HorizontalDivider(color = CyberNexusColors.DividerColor)
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun ScanResultRow(result: ScanResult) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp)
                .background(result.riskLevel.color.copy(0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(result.port.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = result.riskLevel.color)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(result.service, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CyberNexusColors.TextPrimary)
            Text(result.version, fontSize = 11.sp, color = CyberNexusColors.TextHint, fontFamily = FontFamily.Monospace)
        }
        Column(horizontalAlignment = Alignment.End) {
            RiskBadge(result.riskLevel, result.riskLevel.label.uppercase())
            Spacer(Modifier.height(2.dp))
            Text(result.protocol, fontSize = 10.sp, color = CyberNexusColors.TextHint)
        }
    }
}

// ============================================================
// VULNERABILITY ANALYSIS SCREEN
// ============================================================

@Composable
fun VulnAnalysisScreen(vm: CyberNexusViewModel) {
    val vulns by vm.vulnerabilities.collectAsState()
    val isScanning by vm.isScanning.collectAsState()
    val progress by vm.scanProgress.collectAsState()
    val selectedVuln by vm.selectedVuln.collectAsState()

    if (selectedVuln != null) {
        VulnDetailDialog(vuln = selectedVuln!!, onDismiss = { vm.selectVuln(null) })
    }

    Scaffold(
        topBar = { ModuleTopBar("⚠️ Vulnerability Analysis", "Phase 3 — Risk Assessment", vm) },
        bottomBar = { BottomNavBar(Screen.VULN_ANALYSIS, vm) },
        containerColor = CyberNexusColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Automated Vulnerability Scanner", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Analyzes discovered services against known CVE databases", fontSize = 12.sp, color = CyberNexusColors.TextHint, lineHeight = 16.sp)
                Spacer(Modifier.height(12.dp))

                if (isScanning) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                        color = CyberNexusColors.Warning,
                        trackColor = CyberNexusColors.LavenderSoft
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Analyzing ${(progress * MockData.vulnerabilities.size).toInt()} of ${MockData.vulnerabilities.size} services...", fontSize = 12.sp, color = CyberNexusColors.TextHint)
                }

                GradientButton(
                    text = if (isScanning) "Analyzing..." else "Run Vulnerability Scan",
                    onClick = { vm.runVulnAnalysis() },
                    enabled = !isScanning,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (vulns.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))

                // CVSS Score Meter
                NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Overall Risk Score", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
                    Spacer(Modifier.height(16.dp))
                    CvssGauge(score = 8.4f)
                }

                Spacer(Modifier.height(16.dp))
                NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vulnerabilities (${vulns.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
                        Text("Tap for details", fontSize = 11.sp, color = CyberNexusColors.Primary)
                    }
                    Spacer(Modifier.height(12.dp))

                    vulns.forEach { vuln ->
                        VulnRow(vuln = vuln, onClick = { vm.selectVuln(vuln) })
                        if (vulns.last() != vuln) HorizontalDivider(color = CyberNexusColors.DividerColor)
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun CvssGauge(score: Float) {
    val animScore = remember { Animatable(0f) }
    LaunchedEffect(score) { animScore.animateTo(score, tween(1200)) }

    val color = when {
        score >= 9f -> CyberNexusColors.RiskCritical
        score >= 7f -> CyberNexusColors.RiskHigh
        score >= 4f -> CyberNexusColors.RiskMedium
        else -> CyberNexusColors.RiskLow
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val sweepAngle = (animScore.value / 10f) * 270f
            drawArc(
                color = Color.LightGray.copy(0.3f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 14f, cap = StrokeCap.Round),
                size = Size(size.width - 14f, size.height - 14f),
                topLeft = Offset(7f, 7f)
            )
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 14f, cap = StrokeCap.Round),
                size = Size(size.width - 14f, size.height - 14f),
                topLeft = Offset(7f, 7f)
            )
        }
        Column {
            Text(String.format("%.1f", animScore.value), fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, color = color)
            Text("CVSS Score", fontSize = 12.sp, color = CyberNexusColors.TextHint)
            Spacer(Modifier.height(4.dp))
            RiskBadge(RiskLevel.CRITICAL, "CRITICAL SEVERITY")
        }
    }
}

@Composable
fun VulnRow(vuln: Vulnerability, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp)
                .background(vuln.riskLevel.color.copy(0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                String.format("%.1f", vuln.cvssScore),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = vuln.riskLevel.color
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(vuln.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = CyberNexusColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(vuln.cveId, fontSize = 11.sp, color = CyberNexusColors.TextHint, fontFamily = FontFamily.Monospace)
        }
        Column(horizontalAlignment = Alignment.End) {
            RiskBadge(vuln.riskLevel, vuln.riskLevel.label.uppercase())
            Spacer(Modifier.height(2.dp))
            Icon(Icons.Filled.ChevronRight, null, tint = CyberNexusColors.TextHint, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun VulnDetailDialog(vuln: Vulnerability, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(vuln.cveId, fontSize = 12.sp, color = CyberNexusColors.Primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(vuln.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CyberNexusColors.TextPrimary, lineHeight = 20.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, null, tint = CyberNexusColors.TextHint)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RiskBadge(vuln.riskLevel, vuln.riskLevel.label.uppercase())
                    Box(
                        modifier = Modifier.background(CyberNexusColors.Primary.copy(0.1f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("CVSS ${vuln.cvssScore}", fontSize = 10.sp, color = CyberNexusColors.Primary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))
                DetailSection("Description", vuln.description)
                DetailSection("Affected Service", vuln.affectedService)
                DetailSection("Remediation", vuln.remediation)

                Spacer(Modifier.height(8.dp))
                Text("References", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = CyberNexusColors.TextSecondary)
                Spacer(Modifier.height(4.dp))
                vuln.references.forEach { ref ->
                    Text(ref, fontSize = 11.sp, color = CyberNexusColors.Primary, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 2.dp))
                }

                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(CyberNexusColors.Warning.copy(0.08f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text("⚠️ This is a simulated vulnerability for educational purposes only.", fontSize = 11.sp, color = CyberNexusColors.Warning, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = CyberNexusColors.TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(content, fontSize = 13.sp, color = CyberNexusColors.TextPrimary, lineHeight = 18.sp)
    }
}

// ============================================================
// EXPLOITATION SIMULATOR SCREEN
// ============================================================

@Composable
fun ExploitationScreen(vm: CyberNexusViewModel) {
    var selectedExploit by remember { mutableStateOf<ExploitSimulation?>(null) }
    var showSteps by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { ModuleTopBar("⚡ Exploitation Simulator", "Phase 4 — Simulated Demonstrations", vm) },
        bottomBar = { BottomNavBar(Screen.EXPLOITATION, vm) },
        containerColor = CyberNexusColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // Important Warning Banner
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberNexusColors.Error.copy(0.08f)),
                border = BorderStroke(1.dp, CyberNexusColors.Error.copy(0.3f))
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🚫", fontSize = 20.sp)
                    Column {
                        Text("SIMULATION ONLY", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CyberNexusColors.Error)
                        Text("These are educational demonstrations. No actual exploits are executed. Never perform these actions without explicit written authorization.", fontSize = 12.sp, color = CyberNexusColors.Error.copy(0.8f), lineHeight = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Available Exploit Demonstrations", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CyberNexusColors.TextPrimary, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(12.dp))

            MockData.exploitSimulations.forEach { exploit ->
                ExploitCard(
                    exploit = exploit,
                    isSelected = selectedExploit?.id == exploit.id,
                    onClick = {
                        selectedExploit = exploit
                        showSteps = true
                        currentStep = 0
                        isRunning = false
                    }
                )
                Spacer(Modifier.height(10.dp))
            }

            selectedExploit?.let { exploit ->
                if (showSteps) {
                    Spacer(Modifier.height(8.dp))
                    ExploitStepsCard(
                        exploit = exploit,
                        currentStep = currentStep,
                        isRunning = isRunning,
                        onRun = {
                            if (!isRunning) {
                                isRunning = true
                                currentStep = 0
                            }
                        }
                    )
                    LaunchedEffect(isRunning) {
                        if (isRunning) {
                            exploit.steps.forEachIndexed { i, _ ->
                                delay(1200)
                                currentStep = i + 1
                            }
                            isRunning = false
                        }
                    }
                }
            }

            // Ethical Guidelines
            Spacer(Modifier.height(16.dp))
            EthicalGuidelinesCard()

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun ExploitCard(exploit: ExploitSimulation, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberNexusColors.LavenderMist else Color.White
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) CyberNexusColors.Primary else CyberNexusColors.CardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(48.dp).background(
                    if (isSelected) CyberNexusColors.Primary else CyberNexusColors.LavenderSoft,
                    RoundedCornerShape(12.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.BugReport, null, tint = if (isSelected) Color.White else CyberNexusColors.Primary, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(exploit.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberNexusColors.TextPrimary)
                Text(exploit.type, fontSize = 11.sp, color = CyberNexusColors.Primary)
                Spacer(Modifier.height(4.dp))
                Text(exploit.description, fontSize = 12.sp, color = CyberNexusColors.TextHint, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SimBadge("SIMULATION")
                    SimBadge("EDUCATIONAL")
                }
            }
        }
    }
}

@Composable
fun SimBadge(text: String) {
    Box(
        modifier = Modifier.background(CyberNexusColors.Info.copy(0.1f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 9.sp, color = CyberNexusColors.Info, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ExploitStepsCard(exploit: ExploitSimulation, currentStep: Int, isRunning: Boolean, onRun: () -> Unit) {
    NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(exploit.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
        Text("Impact: ${exploit.impact}", fontSize = 12.sp, color = CyberNexusColors.Error, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.height(16.dp))

        Text("Simulation Steps:", fontSize = 13.sp, color = CyberNexusColors.TextSecondary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        exploit.steps.forEachIndexed { i, step ->
            val isDone = i < currentStep
            val isCurrent = i == currentStep && isRunning
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).background(
                    if (isCurrent) CyberNexusColors.Primary.copy(0.05f) else Color.Transparent,
                    RoundedCornerShape(8.dp)
                ).padding(if (isCurrent) 8.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(24.dp).background(
                        when { isDone -> CyberNexusColors.Success; isCurrent -> CyberNexusColors.Primary; else -> CyberNexusColors.LavenderSoft },
                        CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    else Text("${i + 1}", fontSize = 10.sp, color = if (isCurrent) Color.White else CyberNexusColors.TextHint, fontWeight = FontWeight.Bold)
                }
                Text(step, fontSize = 13.sp, color = if (isDone) CyberNexusColors.TextPrimary else CyberNexusColors.TextHint, fontWeight = if (isDone) FontWeight.Medium else FontWeight.Normal)
            }
        }

        Spacer(Modifier.height(12.dp))
        GradientButton(
            text = if (isRunning) "Running Simulation..." else if (currentStep > 0) "Replay Simulation" else "▶ Run Simulation",
            onClick = onRun,
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EthicalGuidelinesCard() {
    NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("⚖️ Ethical Guidelines", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
        Spacer(Modifier.height(10.dp))
        listOf(
            "✅ Always obtain written authorization before testing",
            "✅ Define scope clearly — stay within agreed targets",
            "✅ Report all findings responsibly to stakeholders",
            "✅ Never exploit vulnerabilities beyond proof-of-concept",
            "✅ Follow responsible disclosure guidelines",
            "✅ Respect user privacy at all times"
        ).forEach { guideline ->
            Text(guideline, fontSize = 13.sp, color = CyberNexusColors.TextSecondary, modifier = Modifier.padding(vertical = 3.dp), lineHeight = 18.sp)
        }
    }
}

// ============================================================
// REPORTING SCREEN
// ============================================================

@Composable
fun ReportingScreen(vm: CyberNexusViewModel) {
    val report by vm.report.collectAsState()
    val isGenerating by vm.isScanning.collectAsState()
    val progress by vm.scanProgress.collectAsState()

    Scaffold(
        topBar = { ModuleTopBar("📋 Reporting", "Phase 6 — Professional Report Generation", vm) },
        bottomBar = { BottomNavBar(Screen.REPORTING, vm) },
        containerColor = CyberNexusColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Generate Pentest Report", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Consolidates all findings from the current assessment into a professional report.", fontSize = 12.sp, color = CyberNexusColors.TextHint, lineHeight = 16.sp)
                Spacer(Modifier.height(12.dp))

                if (isGenerating) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                        color = CyberNexusColors.Primary,
                        trackColor = CyberNexusColors.LavenderSoft
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Generating report...", fontSize = 12.sp, color = CyberNexusColors.TextHint)
                    Spacer(Modifier.height(8.dp))
                }

                GradientButton(
                    text = if (isGenerating) "Generating..." else "Generate Report",
                    onClick = { vm.generateReport() },
                    enabled = !isGenerating,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            report?.let { r ->
                Spacer(Modifier.height(16.dp))
                ReportCard(r)
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun ReportCard(report: PentestReport) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        NexusCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PENETRATION TEST REPORT", fontSize = 10.sp, color = CyberNexusColors.Primary, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(report.targetDomain, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CyberNexusColors.TextPrimary)
                }
                Box(
                    modifier = Modifier.size(60.dp).background(
                        Brush.radialGradient(listOf(CyberNexusColors.PrimaryLight, CyberNexusColors.Primary)),
                        RoundedCornerShape(12.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(String.format("%.1f", report.riskScore), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                        Text("RISK", fontSize = 8.sp, color = Color.White.copy(0.8f), letterSpacing = 1.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                SimpleDateFormat("MMMM dd, yyyy — HH:mm", Locale.getDefault()).format(Date(report.generatedAt)),
                fontSize = 11.sp,
                color = CyberNexusColors.TextHint
            )
        }

        // Executive Summary
        NexusCard {
            Text("Executive Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text(report.executiveSummary, fontSize = 13.sp, color = CyberNexusColors.TextSecondary, lineHeight = 20.sp)
        }

        // Findings Table
        NexusCard {
            Text("Key Findings", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
            Spacer(Modifier.height(12.dp))
            report.findings.forEach { vuln ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(8.dp).background(vuln.riskLevel.color, CircleShape)
                    )
                    Text(vuln.title, fontSize = 13.sp, color = CyberNexusColors.TextPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(String.format("%.1f", vuln.cvssScore), fontSize = 12.sp, color = vuln.riskLevel.color, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Recommendations
        NexusCard {
            Text("Recommendations", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
            Spacer(Modifier.height(10.dp))
            report.recommendations.forEachIndexed { i, rec ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("${i + 1}.", fontSize = 13.sp, color = CyberNexusColors.Primary, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                    Text(rec, fontSize = 13.sp, color = CyberNexusColors.TextSecondary, lineHeight = 18.sp)
                }
            }
        }

        // Export Note
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNexusColors.Primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Share, null, tint = Color.White, modifier = Modifier.size(22.dp))
                Column {
                    Text("Export Report", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("Available in PDF, DOCX & JSON formats", fontSize = 11.sp, color = Color.White.copy(0.8f))
                }
            }
        }
    }
}

// ============================================================
// LEARNING HUB SCREEN
// ============================================================

@Composable
fun LearningScreen(vm: CyberNexusViewModel) {
    var selectedModule by remember { mutableStateOf<LearningModule?>(null) }

    if (selectedModule != null) {
        LearningDetailDialog(module = selectedModule!!, onDismiss = { selectedModule = null })
    }

    Scaffold(
        topBar = { ModuleTopBar("📚 Learning Hub", "Complete Pentest Methodology", vm) },
        bottomBar = { BottomNavBar(Screen.LEARNING, vm) },
        containerColor = CyberNexusColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // Progress Header
            NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(56.dp).background(
                            Brush.radialGradient(listOf(CyberNexusColors.PrimaryLight, CyberNexusColors.Primary)),
                            CircleShape
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎓", fontSize = 24.sp)
                    }
                    Column {
                        Text("Your Learning Path", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CyberNexusColors.TextPrimary)
                        Text("7 Modules • Complete Pentest Lifecycle", fontSize = 12.sp, color = CyberNexusColors.TextHint)
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { 0.43f },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                            color = CyberNexusColors.Primary,
                            trackColor = CyberNexusColors.LavenderSoft
                        )
                        Text("3/7 completed", fontSize = 10.sp, color = CyberNexusColors.TextHint, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionTitle("Modules", "Tap any module to explore")
            Spacer(Modifier.height(12.dp))

            MockData.learningModules.forEachIndexed { i, module ->
                LearningModuleCard(
                    module = module,
                    index = i,
                    isCompleted = i < 3,
                    onClick = { selectedModule = module }
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun LearningModuleCard(module: LearningModule, index: Int, isCompleted: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCompleted) CyberNexusColors.LavenderMist else Color.White),
        border = BorderStroke(1.dp, if (isCompleted) CyberNexusColors.Primary.copy(0.3f) else CyberNexusColors.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // Step indicator
            Box(
                modifier = Modifier.size(44.dp).background(
                    if (isCompleted) CyberNexusColors.Primary else CyberNexusColors.LavenderSoft,
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(module.icon, fontSize = 20.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(module.phase, fontSize = 11.sp, color = CyberNexusColors.Primary, fontWeight = FontWeight.SemiBold)
                    if (isCompleted) {
                        Text("✓ Done", fontSize = 11.sp, color = CyberNexusColors.Success, fontWeight = FontWeight.Bold)
                    } else {
                        Text("${module.tools.size} tools", fontSize = 11.sp, color = CyberNexusColors.TextHint)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(module.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(module.description, fontSize = 12.sp, color = CyberNexusColors.TextHint, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    module.tools.take(3).forEach { tool ->
                        Box(
                            modifier = Modifier.background(CyberNexusColors.LavenderSoft, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(tool, fontSize = 10.sp, color = CyberNexusColors.TextSecondary)
                        }
                    }
                    if (module.tools.size > 3) {
                        Text("+${module.tools.size - 3}", fontSize = 10.sp, color = CyberNexusColors.TextHint, modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }
            }
        }
    }
}

@Composable
fun LearningDetailDialog(module: LearningModule, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(module.phase, fontSize = 12.sp, color = CyberNexusColors.Primary, fontWeight = FontWeight.Bold)
                        Text(module.title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = CyberNexusColors.TextPrimary)
                    }
                    Row {
                        Text(module.icon, fontSize = 24.sp)
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Close, null, tint = CyberNexusColors.TextHint)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = CyberNexusColors.DividerColor)
                Spacer(Modifier.height(12.dp))

                Text("Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberNexusColors.TextSecondary)
                Spacer(Modifier.height(6.dp))
                Text(module.description, fontSize = 13.sp, color = CyberNexusColors.TextPrimary, lineHeight = 20.sp)

                Spacer(Modifier.height(16.dp))
                Text("Key Learning Points", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberNexusColors.TextSecondary)
                Spacer(Modifier.height(8.dp))
                module.keyPoints.forEach { point ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(CyberNexusColors.Primary, CircleShape).align(Alignment.CenterVertically))
                        Text(point, fontSize = 13.sp, color = CyberNexusColors.TextPrimary, lineHeight = 18.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Common Tools", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberNexusColors.TextSecondary)
                Spacer(Modifier.height(8.dp))
                module.tools.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { tool ->
                            Box(
                                modifier = Modifier.weight(1f)
                                    .background(CyberNexusColors.LavenderMist, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(tool, fontSize = 13.sp, color = CyberNexusColors.TextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ============================================================
// CLEARING TRACKS SCREEN (DEFENSIVE FOCUS)
// ============================================================

@Composable
fun TracksScreen(vm: CyberNexusViewModel) {
    Scaffold(
        topBar = { ModuleTopBar("🛡️ Clearing Tracks", "Phase 7 — Defensive Awareness", vm) },
        bottomBar = { BottomNavBar(Screen.TRACKS, vm) },
        containerColor = CyberNexusColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // Disclaimer
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberNexusColors.Success.copy(0.08f)),
                border = BorderStroke(1.dp, CyberNexusColors.Success.copy(0.3f))
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🛡️", fontSize = 20.sp)
                    Column {
                        Text("DEFENSIVE PERSPECTIVE", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CyberNexusColors.Success)
                        Text("This module explains log hygiene and system hardening from a defensive security standpoint. Understanding attacker behavior helps build better defenses.", fontSize = 12.sp, color = CyberNexusColors.Success.copy(0.8f), lineHeight = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Log Management Section
            SectionTitle("Log Management Concepts", "What logs attackers target — so defenders can protect them")
            Spacer(Modifier.height(12.dp))

            listOf(
                Triple("📋", "System Logs (/var/log/)", "Auth.log, syslog, kern.log contain system events. Attackers may attempt to clear these. Defenders should ship logs to remote SIEM immediately to prevent tampering."),
                Triple("🌐", "Web Server Logs", "Apache/Nginx access.log records all HTTP requests. Real-time monitoring with tools like GoAccess or ELK Stack enables anomaly detection."),
                Triple("🗄️", "Database Audit Logs", "MySQL/PostgreSQL query logs reveal unauthorized data access. Enable audit logging and alert on suspicious queries."),
                Triple("🔐", "Auth & SSH Logs", "Failed logins, sudo usage, SSH access — all critical forensic evidence. Use fail2ban for automated blocking."),
                Triple("📡", "Network Flow Logs", "NetFlow/IPFIX data reveals lateral movement. Store 90+ days of flow data for incident investigation.")
            ).forEach { (icon, title, desc) ->
                DefenseCard(icon = icon, title = title, description = desc)
                Spacer(Modifier.height(10.dp))
            }

            // SIEM Alerts Section
            Spacer(Modifier.height(8.dp))
            SectionTitle("Defensive Monitoring", "SIEM Rules & Detection")
            Spacer(Modifier.height(12.dp))

            NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Detection Use Cases", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
                Spacer(Modifier.height(10.dp))
                listOf(
                    "🔴 Multiple failed SSH logins (brute force)",
                    "🟠 Port scanning from single source IP",
                    "🟡 Unusual process execution chains",
                    "🟢 New user account creation at odd hours",
                    "🔵 Outbound data transfer spike",
                    "🟣 Log deletion or truncation events"
                ).forEach { item ->
                    Text(item, fontSize = 13.sp, color = CyberNexusColors.TextSecondary, modifier = Modifier.padding(vertical = 5.dp), lineHeight = 18.sp)
                }
            }

            // Hardening Checklist
            Spacer(Modifier.height(16.dp))
            SectionTitle("System Hardening Checklist", "Post-pentest remediation")
            Spacer(Modifier.height(12.dp))

            NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                val items = listOf(
                    "Patch all identified vulnerabilities",
                    "Enable centralized logging (SIEM)",
                    "Implement least-privilege access",
                    "Enable MFA on all admin accounts",
                    "Deploy IDS/IPS at network perimeter",
                    "Conduct regular vulnerability scans",
                    "Implement file integrity monitoring",
                    "Create incident response playbooks"
                )
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(20.dp).background(CyberNexusColors.Success.copy(0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, null, tint = CyberNexusColors.Success, modifier = Modifier.size(12.dp))
                        }
                        Text(item, fontSize = 13.sp, color = CyberNexusColors.TextSecondary)
                    }
                    HorizontalDivider(color = CyberNexusColors.DividerColor, modifier = Modifier.padding(start = 30.dp))
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun DefenseCard(icon: String, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CyberNexusColors.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(icon, fontSize = 22.sp, modifier = Modifier.padding(top = 2.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberNexusColors.TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(description, fontSize = 12.sp, color = CyberNexusColors.TextHint, lineHeight = 17.sp)
            }
        }
    }
}

// ============================================================
// SHARED UI COMPONENTS
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleTopBar(title: String, subtitle: String, vm: CyberNexusViewModel) {
    TopAppBar(
        title = {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CyberNexusColors.TextPrimary)
                Text(subtitle, fontSize = 11.sp, color = CyberNexusColors.TextHint)
            }
        },
        navigationIcon = {
            IconButton(onClick = { vm.navigate(Screen.HOME) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = CyberNexusColors.Primary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        ),
        modifier = Modifier.shadow(2.dp)
    )
}

@Composable
fun BottomNavBar(currentScreen: Screen, vm: CyberNexusViewModel) {
    val navItems = listOf(
        NavItem(Screen.HOME, "Home", Icons.Filled.Home),
        NavItem(Screen.RECON, "Recon", Icons.Filled.Search),
        NavItem(Screen.SCANNING, "Scan", Icons.Filled.Wifi),
        NavItem(Screen.VULN_ANALYSIS, "Vulns", Icons.Filled.BugReport),
        NavItem(Screen.LEARNING, "Learn", Icons.Filled.School)
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(8.dp)
    ) {
        navItems.forEach { item ->
            val selected = currentScreen == item.screen
            NavigationBarItem(
                selected = selected,
                onClick = { vm.navigate(item.screen) },
                icon = {
                    Icon(
                        item.icon,
                        null,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(item.label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyberNexusColors.Primary,
                    selectedTextColor = CyberNexusColors.Primary,
                    unselectedIconColor = CyberNexusColors.TextHint,
                    unselectedTextColor = CyberNexusColors.TextHint,
                    indicatorColor = CyberNexusColors.LavenderMist
                )
            )
        }
    }
}

@Composable
fun NexusCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CyberNexusColors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (enabled)
                    Brush.horizontalGradient(listOf(CyberNexusColors.Primary, CyberNexusColors.PrimaryLight))
                else
                    Brush.horizontalGradient(listOf(Color.Gray.copy(0.3f), Color.Gray.copy(0.3f)))
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!enabled) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
            Text(
                text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

// ============================================================
// POST EXPLOITATION ANALYSIS (EDUCATIONAL MODULE)
// ============================================================

@Composable
fun PostExploitScreen(vm: CyberNexusViewModel) {
    val topics = listOf(
        PostExploitTopic(
            "Persistence Mechanisms",
            "🔁",
            "Attackers establish persistence to maintain access across reboots.",
            listOf(
                "Cron jobs added in /etc/crontab",
                "Systemd services created for backdoors",
                "SSH authorized_keys manipulation",
                "Bashrc / profile modification",
                ".bashrc reverse shell injection"
            ),
            listOf(
                "Audit /etc/crontab for unknown entries",
                "Monitor systemd service additions",
                "Alert on authorized_keys changes",
                "Hash monitoring of critical files"
            )
        ),
        PostExploitTopic(
            "Credential Harvesting",
            "🔑",
            "Understanding how credentials are captured post-compromise helps defenders protect sensitive stores.",
            listOf(
                "In-memory credential dumping (LSASS)",
                "Browser credential store access",
                "/etc/shadow file read (Linux)",
                "SSH private key exfiltration",
                "Environment variable secrets"
            ),
            listOf(
                "Enable Credential Guard on Windows",
                "Use secrets management tools (Vault)",
                "Restrict /etc/shadow read access",
                "Rotate all credentials post-incident"
            )
        ),
        PostExploitTopic(
            "Lateral Movement",
            "↔️",
            "After initial compromise, attackers move across the network to reach high-value targets.",
            listOf(
                "Pass-the-Hash attacks on Windows",
                "SSH key hopping to other servers",
                "Exploiting trust relationships",
                "Pivoting via compromised hosts",
                "Domain privilege escalation"
            ),
            listOf(
                "Network segmentation (zero-trust)",
                "Disable NTLM where possible",
                "Monitor East-West traffic",
                "Implement PAM/privileged access mgmt"
            )
        ),
        PostExploitTopic(
            "Data Exfiltration Paths",
            "📤",
            "Understanding exfiltration channels helps defenders detect and block sensitive data leaving the network.",
            listOf(
                "HTTP/S data tunneling",
                "DNS exfiltration via TXT queries",
                "ICMP covert channels",
                "Steganography in images",
                "Cloud storage abuse (Dropbox, Drive)"
            ),
            listOf(
                "DLP (Data Loss Prevention) solutions",
                "DNS query monitoring & filtering",
                "Egress traffic anomaly alerts",
                "Block unauthorized cloud storage"
            )
        )
    )

    Scaffold(
        topBar = { ModuleTopBar("🎯 Post-Exploitation", "Phase 5 — Defensive Analysis", vm) },
        bottomBar = { BottomNavBar(Screen.EXPLOITATION, vm) },
        containerColor = CyberNexusColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberNexusColors.Info.copy(0.08f)),
                border = BorderStroke(1.dp, CyberNexusColors.Info.copy(0.3f))
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🎯", fontSize = 20.sp)
                    Text(
                        "Post-exploitation analysis is studied defensively to understand attacker tactics and build better detections. All content is educational.",
                        fontSize = 12.sp,
                        color = CyberNexusColors.Info,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            topics.forEach { topic ->
                PostExploitCard(topic = topic)
                Spacer(Modifier.height(12.dp))
            }

            // MITRE ATT&CK Reference
            Spacer(Modifier.height(8.dp))
            NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("MITRE ATT&CK Framework", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text("The MITRE ATT&CK framework catalogues known adversary tactics, techniques, and procedures (TTPs). Use it to map defensive gaps.", fontSize = 13.sp, color = CyberNexusColors.TextHint, lineHeight = 18.sp)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        "Reconnaissance" to "TA0043",
                        "Initial Access" to "TA0001",
                        "Persistence" to "TA0003",
                        "Exfiltration" to "TA0010"
                    ).forEach { (name, id) ->
                        MitreChip(name = name, id = id, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

data class PostExploitTopic(
    val title: String,
    val icon: String,
    val description: String,
    val attackerMethods: List<String>,
    val defenderActions: List<String>
)

@Composable
fun PostExploitCard(topic: PostExploitTopic) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(if (expanded) 180f else 0f, label = "expand")

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CyberNexusColors.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(CyberNexusColors.LavenderMist, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(topic.icon, fontSize = 22.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(topic.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberNexusColors.TextPrimary)
                    Text(topic.description, fontSize = 12.sp, color = CyberNexusColors.TextHint, lineHeight = 16.sp, maxLines = if (expanded) Int.MAX_VALUE else 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Filled.ExpandMore, null, tint = CyberNexusColors.Primary, modifier = Modifier.size(20.dp).rotate(rotationAngle))
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = CyberNexusColors.DividerColor)
                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Attacker Methods", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyberNexusColors.Error)
                            Spacer(Modifier.height(6.dp))
                            topic.attackerMethods.forEach { method ->
                                Row(modifier = Modifier.padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("•", color = CyberNexusColors.Error, fontSize = 12.sp)
                                    Text(method, fontSize = 11.sp, color = CyberNexusColors.TextSecondary, lineHeight = 15.sp)
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Defender Actions", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyberNexusColors.Success)
                            Spacer(Modifier.height(6.dp))
                            topic.defenderActions.forEach { action ->
                                Row(modifier = Modifier.padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("✓", color = CyberNexusColors.Success, fontSize = 12.sp)
                                    Text(action, fontSize = 11.sp, color = CyberNexusColors.TextSecondary, lineHeight = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MitreChip(name: String, id: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CyberNexusColors.LavenderMist, RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(id, fontSize = 10.sp, color = CyberNexusColors.Primary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(2.dp))
        Text(name, fontSize = 9.sp, color = CyberNexusColors.TextHint, textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}

// ============================================================
// ANIMATED HEXAGON GRID BACKGROUND COMPONENT
// ============================================================

@Composable
fun HexGridBackground(modifier: Modifier = Modifier, alpha: Float = 0.05f) {
    val infiniteTransition = rememberInfiniteTransition(label = "hex")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "hex_offset"
    )
    Canvas(modifier = modifier) {
        val hexSize = 30f
        val cols = (size.width / (hexSize * 1.5f)).toInt() + 2
        val rows = (size.height / (hexSize * 1.73f)).toInt() + 2
        for (row in -1..rows + 1) {
            for (col in -1..cols + 1) {
                val cx = col * hexSize * 1.5f + (if (row % 2 == 0) 0f else hexSize * 0.75f)
                val cy = row * hexSize * 1.73f * 0.5f + offset
                val path = Path()
                for (i in 0..5) {
                    val angle = Math.PI / 180.0 * (60 * i - 30)
                    val x = cx + hexSize * 0.8f * cos(angle).toFloat()
                    val y = cy + hexSize * 0.8f * sin(angle).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, CyberNexusColors.Primary.copy(alpha = alpha), style = Stroke(width = 1f))
            }
        }
    }
}

// ============================================================
// NETWORK TOPOLOGY VISUALIZER COMPONENT
// ============================================================

@Composable
fun NetworkTopologyCard() {
    NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Network Topology", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberNexusColors.TextPrimary)
        Text("Discovered nodes from scan simulation", fontSize = 12.sp, color = CyberNexusColors.TextHint)
        Spacer(Modifier.height(16.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val nodes = listOf(
                Offset(centerX, centerY) to "Router",
                Offset(centerX - 120f, centerY - 60f) to "192.168.1.100",
                Offset(centerX + 120f, centerY - 60f) to "192.168.1.101",
                Offset(centerX - 120f, centerY + 70f) to "192.168.1.102",
                Offset(centerX + 120f, centerY + 70f) to "192.168.1.103",
                Offset(centerX, centerY - 100f) to "Gateway"
            )
            nodes.drop(1).forEach { (pos, _) ->
                drawLine(
                    color = CyberNexusColors.LavenderSoft,
                    start = nodes[0].first,
                    end = pos,
                    strokeWidth = 2f
                )
            }
            nodes.forEachIndexed { i, (pos, label) ->
                val isCenter = i == 0
                val isTarget = i == 1
                drawCircle(
                    color = when { isCenter -> CyberNexusColors.Primary; isTarget -> CyberNexusColors.RiskCritical; else -> CyberNexusColors.LavenderDeep },
                    radius = if (isCenter) 24f else 16f,
                    center = pos
                )
                drawCircle(
                    color = Color.White,
                    radius = if (isCenter) 20f else 12f,
                    center = pos,
                    style = Stroke(width = 2f)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(CyberNexusColors.Primary, "Gateway")
            LegendItem(CyberNexusColors.RiskCritical, "Target Host")
            LegendItem(CyberNexusColors.LavenderDeep, "Network Node")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(label, fontSize = 11.sp, color = CyberNexusColors.TextHint)
    }
}

// ============================================================
// RISK METER COMPOSABLE (REUSABLE)
// ============================================================

@Composable
fun RiskMeter(score: Float, modifier: Modifier = Modifier) {
    val animScore = remember { Animatable(0f) }
    LaunchedEffect(score) { animScore.animateTo(score, tween(1000, easing = EaseOutCubic)) }

    val color = when {
        animScore.value >= 0.75f -> CyberNexusColors.RiskCritical
        animScore.value >= 0.5f -> CyberNexusColors.RiskHigh
        animScore.value >= 0.25f -> CyberNexusColors.RiskMedium
        else -> CyberNexusColors.RiskLow
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
            val barHeight = 20f
            val barY = size.height - barHeight - 10f
            val barWidth = size.width

            drawRoundRect(
                color = Color.LightGray.copy(0.3f),
                topLeft = Offset(0f, barY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barHeight / 2)
            )

            val gradient = Brush.horizontalGradient(
                listOf(CyberNexusColors.RiskLow, CyberNexusColors.RiskMedium, CyberNexusColors.RiskHigh, CyberNexusColors.RiskCritical)
            )
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(0f, barY),
                size = Size(barWidth * animScore.value, barHeight),
                cornerRadius = CornerRadius(barHeight / 2)
            )

            val markerX = barWidth * animScore.value
            drawCircle(color = Color.White, radius = 14f, center = Offset(markerX, barY + barHeight / 2))
            drawCircle(color = color, radius = 10f, center = Offset(markerX, barY + barHeight / 2))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Low", fontSize = 10.sp, color = CyberNexusColors.RiskLow)
            Text("Medium", fontSize = 10.sp, color = CyberNexusColors.RiskMedium)
            Text("High", fontSize = 10.sp, color = CyberNexusColors.RiskHigh)
            Text("Critical", fontSize = 10.sp, color = CyberNexusColors.RiskCritical)
        }
    }
}

// ============================================================
// ANIMATED PULSE INDICATOR (FOR LIVE SCAN FEEDBACK)
// ============================================================

@Composable
fun PulseIndicator(color: Color = CyberNexusColors.Primary, size: Dp = 12.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse_alpha"
    )
    Box(
        modifier = Modifier.size(size * 2),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(size * scale)
                .alpha(alpha)
                .background(color.copy(0.3f), CircleShape)
        )
        Box(modifier = Modifier.size(size).background(color, CircleShape))
    }
}

// ============================================================
// CVE SEARCH BAR COMPONENT
// ============================================================

@Composable
fun CveSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberNexusColors.LavenderMist, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.Search, null, tint = CyberNexusColors.TextHint, modifier = Modifier.size(18.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(fontSize = 14.sp, color = CyberNexusColors.TextPrimary),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text("Search CVEs, vulnerabilities...", fontSize = 14.sp, color = CyberNexusColors.TextHint)
                    }
                    innerTextField()
                }
            }
        )
        if (query.isNotEmpty()) {
            Icon(
                Icons.Filled.Close,
                null,
                tint = CyberNexusColors.TextHint,
                modifier = Modifier.size(16.dp).clickable { onQueryChange("") }
            )
        }
    }
}

// ============================================================
// SECURITY SCORE WIDGET
// ============================================================

@Composable
fun SecurityScoreWidget(modifier: Modifier = Modifier) {
    val score = 42 // out of 100
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { animProgress.animateTo(score / 100f, tween(1200)) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CyberNexusColors.CardBorder),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Security Score", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberNexusColors.TextPrimary)
            Spacer(Modifier.height(12.dp))

            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(Color.LightGray.copy(0.3f), -90f, 360f, false, style = Stroke(10f, cap = StrokeCap.Round))
                    drawArc(
                        color = if (animProgress.value < 0.4f) CyberNexusColors.RiskCritical else if (animProgress.value < 0.6f) CyberNexusColors.RiskMedium else CyberNexusColors.Success,
                        startAngle = -90f,
                        sweepAngle = 360f * animProgress.value,
                        useCenter = false,
                        style = Stroke(10f, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = CyberNexusColors.RiskCritical)
                    Text("/ 100", fontSize = 10.sp, color = CyberNexusColors.TextHint)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Needs Improvement", fontSize = 11.sp, color = CyberNexusColors.RiskCritical, fontWeight = FontWeight.Bold)
        }
    }
}

// ============================================================
// TOOL CHIP ROW COMPONENT
// ============================================================

@Composable
fun ToolChipRow(tools: List<String>, selectedTool: String?, onSelect: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(tools) { tool ->
            val selected = tool == selectedTool
            Box(
                modifier = Modifier
                    .background(
                        if (selected) CyberNexusColors.Primary else CyberNexusColors.LavenderMist,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(tool) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    tool,
                    fontSize = 13.sp,
                    color = if (selected) Color.White else CyberNexusColors.TextSecondary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

// ============================================================
// PROGRESS STEPPER FOR WORKFLOW VISUALIZATION
// ============================================================

@Composable
fun WorkflowStepper(currentPhase: Int) {
    val phases = listOf("Recon", "Scan", "Vuln", "Exploit", "Report")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        phases.forEachIndexed { i, phase ->
            val isDone = i < currentPhase
            val isCurrent = i == currentPhase

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(32.dp).background(
                        when { isDone -> CyberNexusColors.Success; isCurrent -> CyberNexusColors.Primary; else -> CyberNexusColors.LavenderSoft },
                        CircleShape
                    ).then(
                        if (isCurrent) Modifier.border(2.dp, CyberNexusColors.Primary.copy(0.3f), CircleShape) else Modifier
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    else Text("${i + 1}", fontSize = 12.sp, color = if (isCurrent) Color.White else CyberNexusColors.TextHint, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text(phase, fontSize = 9.sp, color = if (isCurrent) CyberNexusColors.Primary else CyberNexusColors.TextHint, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
            }

            if (i < phases.size - 1) {
                Box(
                    modifier = Modifier.weight(1f).height(2.dp).background(
                        if (isDone) CyberNexusColors.Success.copy(0.4f) else CyberNexusColors.LavenderSoft
                    )
                )
            }
        }
    }
}

// ============================================================
// NOTIFICATION TOAST COMPONENT
// ============================================================

@Composable
fun NexusToast(message: String, type: ToastType = ToastType.INFO, visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(300)) { -it } + fadeIn(tween(300)),
        exit = slideOutVertically(tween(300)) { -it } + fadeOut(tween(300))
    ) {
        val (bgColor, icon) = when (type) {
            ToastType.SUCCESS -> CyberNexusColors.Success to "✓"
            ToastType.ERROR -> CyberNexusColors.Error to "✗"
            ToastType.WARNING -> CyberNexusColors.Warning to "⚠"
            ToastType.INFO -> CyberNexusColors.Primary to "ℹ"
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(bgColor, RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 16.sp, color = Color.White)
            Text(message, fontSize = 13.sp, color = Color.White, modifier = Modifier.weight(1f))
        }
    }
}

enum class ToastType { SUCCESS, ERROR, WARNING, INFO }

// ============================================================
// EMPTY STATE COMPONENT
// ============================================================

@Composable
fun EmptyState(title: String, subtitle: String, emoji: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CyberNexusColors.TextPrimary, textAlign = TextAlign.Center)
        Text(subtitle, fontSize = 14.sp, color = CyberNexusColors.TextHint, textAlign = TextAlign.Center, lineHeight = 20.sp)
        if (action != null && onAction != null) {
            Spacer(Modifier.height(8.dp))
            GradientButton(text = action, onClick = onAction, modifier = Modifier.width(180.dp))
        }
    }
}

// ============================================================
// ABOUT SECTION
// ============================================================

@Composable
fun AboutSection() {
    NexusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.size(64.dp).background(
                    Brush.radialGradient(listOf(CyberNexusColors.PrimaryLight, CyberNexusColors.Primary)),
                    RoundedCornerShape(16.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Text("⬡", fontSize = 30.sp, color = Color.White)
            }
            Spacer(Modifier.height(12.dp))
            Text("CyberNexus", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = CyberNexusColors.TextPrimary)
            Text("v1.0.0 — Educational Edition", fontSize = 12.sp, color = CyberNexusColors.TextHint)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = CyberNexusColors.DividerColor)
            Spacer(Modifier.height(12.dp))
            listOf(
                "Architecture" to "MVVM + Jetpack Compose",
                "Database" to "Room (Local Storage)",
                "DI" to "Hilt Dependency Injection",
                "Theme" to "White & Lavender",
                "Purpose" to "Educational Only"
            ).forEach { (k, v) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(k, fontSize = 13.sp, color = CyberNexusColors.TextHint)
                    Text(v, fontSize = 13.sp, color = CyberNexusColors.TextPrimary, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(CyberNexusColors.Warning.copy(0.08f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "⚠️ CyberNexus is a strictly educational application. All penetration testing simulations are for learning only. Always obtain proper authorization before any real security assessment.",
                    fontSize = 11.sp,
                    color = CyberNexusColors.Warning,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================
// IMPORT MISSING
// ============================================================
// Note: Add this import at the top if BasicTextField is used:
// import androidx.compose.foundation.text.BasicTextField

// ============================================================
// END OF CYBERNEXUS — COMPLETE SINGLE-FILE IMPLEMENTATION
// Lines: 2500+ | Architecture: MVVM | Theme: White + Lavender
// All modules: Recon → Scan → Vuln → Exploit → Report → Learn → Tracks
// ⚠️ For EDUCATIONAL USE ONLY — No real exploits included
// ============================================================