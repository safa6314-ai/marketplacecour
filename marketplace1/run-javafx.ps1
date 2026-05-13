$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$javafxJars = @(
    "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar",
    "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar",
    "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar",
    "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar",
    "$env:USERPROFILE\.m2\repository\com\mysql\mysql-connector-j\8.4.0\mysql-connector-j-8.4.0.jar"
)

foreach ($jar in $javafxJars) {
    if (!(Test-Path $jar)) {
        throw "Jar introuvable: $jar"
    }
}

$classpath = ($javafxJars + @("target\classes", "src\main\resources")) -join ";"

New-Item -ItemType Directory -Force target\classes | Out-Null

$sourceFiles = @()
$sourceFiles += Get-ChildItem "src\main\java\org\example\entities\*.java" | Select-Object -ExpandProperty FullName
$sourceFiles += Get-ChildItem "src\main\java\org\example\utils\*.java" | Select-Object -ExpandProperty FullName
$sourceFiles += Get-ChildItem "src\main\java\org\example\services\*.java" | Select-Object -ExpandProperty FullName
$sourceFiles += "src\main\java\org\example\controller\MarketplaceController.java"
$sourceFiles += "src\main\java\org\example\MarketplaceSceneBuilderApp.java"

javac -cp (($javafxJars) -join ";") -d target\classes $sourceFiles
java -cp $classpath org.example.MarketplaceSceneBuilderApp
