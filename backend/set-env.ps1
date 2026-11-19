# CampusBridge Environment Setup Script
# Configures Java 21 for the current PowerShell terminal session

$jdk21Path = "C:\Program Files\Java\jdk-21"

if (Test-Path "$jdk21Path\bin\java.exe") {
    $env:JAVA_HOME = $jdk21Path
    $env:Path = "$jdk21Path\bin;$env:Path"
    Write-Host "CampusBridge: Active Java runtime set to Java 21 ($jdk21Path)" -ForegroundColor Green
    & java -version
} else {
    Write-Warning "JDK 21 not found at $jdk21Path. Please verify your Java 21 installation path."
}
