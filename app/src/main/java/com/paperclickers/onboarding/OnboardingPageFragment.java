/*
 * Paperclickers - Affordable solution for classroom response system.
 *
 * Copyright (C) 2015-2018 Eduardo Valle Jr <dovalle@dca.fee.unicamp.br>
 * Copyright (C) 2015-2018 Eduardo Seiti de Oliveira <eduseiti@dca.fee.unicamp.br>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package com.paperclickers.onboarding;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.paperclickers.R;
import com.paperclickers.log;

/**
 * Created by eduseiti on 04/02/18.
 */

public class OnboardingPageFragment extends Fragment {

    static String TAG = "OnboardingPageFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int position = getArguments().getInt("position", 0);

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.onboarding_page, container, false);

        ImageView pageImage = (ImageView) rootView.findViewById(R.id.onboarding_page_image);

        Context appContext = getActivity().getApplicationContext();

        Drawable imageToUse;

        switch (position) {
            case 1:
                imageToUse = getResources().getDrawable(R.drawable.icon_b);

                break;

            case 2:
                imageToUse = getResources().getDrawable(R.drawable.icon_c);

                break;

            default:
                imageToUse = getResources().getDrawable(R.drawable.icon_a);
        }

        pageImage.setImageDrawable(imageToUse);

        log.d(TAG, "onCreateView - Created the onboarding fragment!!! Position: " + position);

        return rootView;
    }
}
