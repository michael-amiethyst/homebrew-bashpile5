class Bashpile < Formula
  desc "The Bash Transpiler: Write in a modern language and run in a Bash5 shell!"
  version "0.22.0"
  homepage "https://github.com/michael-amiethyst/homebrew-bashpile5"
  license "MIT"
  url "https://github.com/michael-amiethyst/homebrew-bashpile5", using: :git, branch: "main", tag: "0.22.0"
  head "https://github.com/michael-amiethyst/homebrew-bashpile5", using: :git, branch: "development"

  # foundational dependencies
  depends_on "make" => :build
  depends_on "gradle@9" => :build
  depends_on "bc"
  depends_on "gnu-sed"
  depends_on "gsed"
  depends_on "kotlin"
  depends_on "openjdk@21"
  depends_on "bash"
  depends_on "gawk"
  depends_on "shfmt"

  # tooling dependencies for generated scripts
  # depends_on "gnu-getopt" # needed for OSX and FreeBSD, kept as generic dependency for consistency

  def install
    system "./gradlew clean build -x test -x integrationTest -x nativeCompile --stacktrace"
    bin.install "build/bashpile"
  end

  test do
    assert_match "6.28", shell_output("#{bin}/bashpile -c \"print(3.14 + 3.14)\"")
  end

  def caveats
    <<~EOS
      Ensure the installed Bash is at least version 5 and is the default Bash.
    EOS
  end
end
