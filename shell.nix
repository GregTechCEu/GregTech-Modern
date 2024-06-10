with import <nixpkgs> {};
let
    libs = [ 
        pkgs.glfw 
        pkgs.libGL
        pkgs.libpulseaudio
    ];

    existingLDPath = builtins.getEnv "LD_LIBRARY_PATH";

    LDPath = if builtins.stringLength existingLDPath == 0 then
        "${lib.makeLibraryPath libs}"
    else
        "${lib.makeLibraryPath libs}:${existingLDPath}"
    ;
in 
    mkShell {
        buildInputs = libs;
        LD_LIBRARY_PATH = LDPath;
    }
