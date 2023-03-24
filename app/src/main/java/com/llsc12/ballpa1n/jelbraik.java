package com.llsc12.ballpa1n;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.system.Os;
import android.system.StructUtsname;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.llsc12.ballpa1n.databinding.JailbreakFragBinding;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ListIterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class jelbraik extends Fragment {

	private final Vector<String> console_append = new Vector<String>();
	public static final DisplayMetrics display = new DisplayMetrics();
	public static final Random random = new Random();
	private final Handler console_updater = new Handler();
	private JailbreakFragBinding binding;
	public boolean finished = false;
	public boolean jbing = false;
	public int currentStage = 0;

	private final Runnable update_console = () -> {
		boolean scrolled = binding.console.canScrollVertically(1); // If the user has scrolled up

		ListIterator<String> lines = console_append.listIterator();
		while (lines.hasNext()) {
			binding.console.append(lines.next());
			lines.remove();

			binding.console.setVerticalScrollBarEnabled(false);
			while (!scrolled && binding.console.canScrollVertically(1))
				binding.console.scrollBy(0, 10);
			binding.console.setVerticalScrollBarEnabled(true);
		}

		update_console();
	};

	private void update_console() {
		console_updater.postDelayed(update_console, 500);
	}

	public static double randomDouble(double min, double max) {
		return min + (max - min) * random.nextDouble();
	}

	public void consoleLog(String message) {
		console_append.add(message + "\n");
	}
	public void updateStatus() {
		binding.status.setText(getResources().getString(R.string.status, finished ? jbSteps.length : currentStage, jbSteps.length, finished ? "Finished." : jbSteps[currentStage].status));
		binding.progressBar.setProgress((int) ((double) currentStage / (double) max * 100d));
	}

	public void beginJB() {
		if (jbing) return;
		jbing = true;

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.getMainLooper());

		binding.jailbreak.setText(R.string.jailbreaking);
		binding.jailbreak.setBackgroundResource(R.drawable.rounded_button_greyed);
		binding.jailbreak.setTextColor(getResources().getColor(R.color.button_text_greyed, getActivity().getTheme()));
		binding.progressBar.setVisibility(View.VISIBLE);
		binding.console.setVisibility(View.VISIBLE);

		executor.execute(() -> {
			Looper.prepare();
			currentStage = 0;
			for (StageStep step : jbSteps) {
				double waitTime = step.argInterval + randomDouble(-0.2d, 1d);
				if (waitTime < 0) waitTime = 0;

				for (ConsoleStep logItem : step.consoleLogs) {
					double logWait = logItem.delay + randomDouble(-0.2d, 1d);
					if (logWait < 0) logWait = 0;

					try {
						Thread.sleep((int) (logWait * 1000));
					} catch (InterruptedException e) {}

					consoleLog(logItem.line);
				}

				try {
					Thread.sleep((int) (waitTime * 1000));
				} catch (InterruptedException e) {}

				if (currentStage != max)
					currentStage++;
				if (!jbSteps[currentStage].status.isEmpty()) updateStatus();
			}

			finished = true;
			jbing = false;
			updateStatus();

			handler.post(() -> {
				binding.jailbreak.setText(R.string.respring);
				binding.jailbreak.setBackgroundResource(R.drawable.rounded_button);
				binding.jailbreak.setTextColor(getResources().getColor(R.color.background, getActivity().getTheme()));
				consoleLog("[*] Finished! Please respring.");
			});
		});
	}

	public void respring() {
		getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		NavHostFragment.findNavController(this).navigate(R.id.action_FirstFragment_to_SecondFragment);
		console_updater.removeCallbacks(update_console);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = JailbreakFragBinding.inflate(inflater, container, false);
		return binding.getRoot();

	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		StructUtsname uname = Os.uname();
		String version = Build.VERSION.RELEASE;
		if (!version.contains(".")) version += ".0";

		consoleLog("[*] Machine Name: " + Build.MANUFACTURER + " " + Build.PRODUCT);
		consoleLog("[*] Model Name: " + Build.MODEL);
		consoleLog("[*] " +  uname.sysname + " " + uname.release + " " + uname.version + " " + uname.machine);
		consoleLog("[*] System Version: Android " + version);

		updateStatus();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(display);
		binding.progressBar.setVisibility(View.GONE);
		binding.console.setVisibility(View.GONE);
		binding.console.setText("");
		binding.console.setMovementMethod(new ScrollingMovementMethod());
		binding.description.setText(getResources().getString(R.string.description, version));
		binding.console.setHeight(display.heightPixels / 4);

		binding.jailbreak.setOnClickListener(clickListener -> {
			if (finished)
				respring();
			else
				beginJB();
		});

		update_console();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
	public static final StageStep[] jbSteps = new StageStep[] {
		new StageStep("Ready to jailbreak", 0f, new ConsoleStep[] {}),
		new StageStep("Ensuring resources", 0.8f, new ConsoleStep[] {
			new ConsoleStep(0.2f, "[*] Stage (1): Ensuring resources"),
			new ConsoleStep(0.7f, "[+] Ensured resources"),
		}),
		new StageStep("Exploiting kernel", 0.5f, new ConsoleStep[] {
			new ConsoleStep(0.2f, "[*] Stage (2): Exploiting kernel"),
			new ConsoleStep(9, "[+] Exploited kernel"),
		}),
		new StageStep("Initializing", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (3): Initializing"),
		}),
		new StageStep("Finding kernel slide", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (4): Finding kernel slide"),
			new ConsoleStep(0f, "[+] Found kernel slide: 0fx8d8c000f"),
		}),
		new StageStep("Finding kernel offsets", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (5): Finding kernel offsets"),
			new ConsoleStep(0f, "[+] Found kernel offsets"),
		}),
		new StageStep("Finding data structures", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (6): Finding data structures"),
			new ConsoleStep(0f, "[+] Found data structures"),
		}),
		new StageStep("Finding kernel offsets", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (7): Finding kernel offsets"),
			new ConsoleStep(0f, "[+] Found kernel offsets"),
		}),
		new StageStep("Obtaining root privileges", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (8): Obtaining root privileges"),
			new ConsoleStep(0f, "[+] Obtained root privileges"),
		}),
		new StageStep("Disabling sandbox", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (9): Disabling sandbox"),
			new ConsoleStep(0f, "[+] Disabled sandbox"),
		}),
		new StageStep("Updating host port", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (10): Updating host port"),
			new ConsoleStep(0f, "[+] Updated host port"),
		}),
		new StageStep("Finding kernel offsets", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (11): Finding kernel offsets"),
			new ConsoleStep(0f, "[+] Found kernel offsets"),
		}),
		new StageStep("Enabling dynamic codesign", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (12): Enabling dynamic codesign"),
			new ConsoleStep(0f, "[+] Enabled dynamic codesign"),
		}),
		new StageStep("", 0f, new ConsoleStep[] {}),
		new StageStep("", 0f, new ConsoleStep[] {}),
		new StageStep("", 0f, new ConsoleStep[] {}),
		new StageStep("Saving kernel primitives", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (16): Saving kernel primitives"),
			new ConsoleStep(0f, "[+] Saved kernel primitives"),
		}),
		new StageStep("", 0f, new ConsoleStep[] {}),
		new StageStep("Disabling codesigning", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (18): Disabling codesigning"),
			new ConsoleStep(0f, "[+] Disabled codesigning"),
		}),
		new StageStep("Obtaining entitlements", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (19): Obtaining entitlements"),
			new ConsoleStep(0f, "[+] Obtained entitlements"),
		}),
		new StageStep("Purging software updates", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (20): Purging software updates"),
			new ConsoleStep(0f, "[+] Purged software updates"),
		}),
		new StageStep("Setting boot-nonce generator", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (21): Setting boot-nonce generator"),
			new ConsoleStep(0f, "[+] Set boot-nonce generator"),
		}),
		new StageStep("Remounting root filesystem", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (22): Remounting root filesystem"),
			new ConsoleStep(0f, "[+] Remounted root filesystem"),
		}),
		new StageStep("Preparing filesystem", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (23): Preparing filesystem"),
			new ConsoleStep(0.1f, "[+] Enabled code substitution"),
			new ConsoleStep(0f, "[+] Prepared filesystem"),
		}),
		new StageStep("Resolving dependencies", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (24): Resolving dependencies"),
			new ConsoleStep(0.2f,
				"[*] Resource Pkgs: \"(\n" +
					"bzip2,\n" +
					"\"coreutils-bin\",\n" +
					"diffutils,\n" +
					"file,\n" +
					"sed,\n" +
					"findutils,\n" +
					"gzip,\n" +
					"libplist3,\n" +
					"firmware,\n" +
					"\"ca-certificates\",\n" +
					"\"libssl1.1.1\",\n" +
					"ldid,\n" +
					"lzma,\n" +
					"\"ncurses5-libs\"\n" +
					"\"profile.d\",\n" +
					"coreutils,\n" +
					"ncurses,\n" +
					"XZ,\n" +
					"tar,\n" +
					"dpkg,\n" +
					"grep,\n" +
					"readline,\n" +
					"bash,\n" +
					"launchctl,\n" +
					"\"com.ex.substitute\n" +
					")"),
			new ConsoleStep(0.1f, "[+] Resolved dependencies")
		}),
		new StageStep("Verifying dependencies", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (25): Verifying dependencies"),
			new ConsoleStep(1.5f, "[+] File checksums verified"),
			new ConsoleStep(0f, "[*] No errors in verifying checksums"),
		}),
		new StageStep("Unknown", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (26): Unknown"),
		}),
		new StageStep("Preparing resources", 0.5f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (27): Preparing resources"),
			new ConsoleStep(0.2f, "[+] Unpacking"),
		}),
		new StageStep("Unknown", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (28): Unknown"),
		}),
		new StageStep("Bootstrapping resources", 1, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (29): Bootstrapping resources"),
			new ConsoleStep(0.4f, "[+] Copying resources"),
		}),
		new StageStep("Unknown", 0.1f, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (30): Unknown"),
		}),
		new StageStep("Cleaning up", 1, new ConsoleStep[] {
			new ConsoleStep(0.1f, "[*] Stage (31): Cleaning up"),
			new ConsoleStep(0.2f, "[+] Removing temporary files"),
		}),
	};
	public static final int max = jbSteps.length - 1;
}